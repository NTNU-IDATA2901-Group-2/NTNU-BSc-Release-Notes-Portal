import solwrLogo from '@/assets/solwr_logo.svg?raw';
import md from './markdown-it';
import { i18n } from './i18n';
import { getLocaleDateString } from './format-date';
import { jiraTicketUrl } from './jira';
import type { ChangeImpact, ChangeNote, ReleaseNote, ReleaseTimeline } from './types';
import pdfMake from "pdfmake/build/pdfmake";
import vfs from "pdfmake/build/vfs_fonts";
import type { Content, ContentText, TableCell, TDocumentDefinitions } from "pdfmake/interfaces";

const t = (key: string) => i18n.global.t(key);

pdfMake.addVirtualFileSystem(vfs);

// The logo's colors are baked into the SVG's `fill` attributes, so pdfmake's
// `color` option has no effect. Recolor every painted fill to black instead.
const blackLogo = solwrLogo.replace(/fill="(?!none")[^"]*"/g, 'fill="black"');

type Token = ReturnType<typeof md.parse>[number];

// Font sizes for headings authored inside markdown content. `#` (h1) renders
// larger than `##`, and every level below `##` collapses to the `##` size.
// Both stay below the change note heading (NOTE_HEADING_SIZE / the release
// title style) so a `#` in the markdown can never outrank the structure around
// it.
const MARKDOWN_H1_SIZE = 12;
const MARKDOWN_H2_SIZE = 11;
const MARKDOWN_BODY_SIZE = 11;

// The change note label line ("Ticket ID: … Title") in the preview layout.
// Kept above MARKDOWN_H1_SIZE so a markdown heading in the description can't
// render larger than the note's own heading.
const NOTE_HEADING_SIZE = 13;

// Link runs render in this blue with an underline, matching the in-app styling.
const LINK_COLOR = '#0b5cff';

/** A clickable text run linking a Jira issue/service-request key to its browse page. */
function jiraLink(key: string): ContentText {
  return { text: key, link: jiraTicketUrl(key), color: LINK_COLOR, decoration: 'underline' };
}

/**
 * Flattens an `inline` token's children into a pdfmake text run, carrying bold,
 * italic and link styling through nested emphasis.
 */
function inlineToRuns(inline: Token): ContentText[] {
  const runs: ContentText[] = [];
  let bold = 0;
  let italics = 0;
  let link: string | undefined;

  for (const child of inline.children ?? []) {
    switch (child.type) {
      case 'strong_open': bold++; break;
      case 'strong_close': bold--; break;
      case 'em_open': italics++; break;
      case 'em_close': italics--; break;
      case 'link_open': link = child.attrGet('href') ?? undefined; break;
      case 'link_close': link = undefined; break;
      case 'softbreak': runs.push({ text: ' ' }); break;
      case 'hardbreak': runs.push({ text: '\n' }); break;
      case 'text':
      case 'code_inline':
        runs.push({
          text: child.content,
          bold: bold > 0,
          italics: italics > 0,
          ...(link ? { link, color: LINK_COLOR, decoration: 'underline' } : {}),
        });
        break;
    }
  }
  return runs;
}

/**
 * Walks a flat markdown-it token stream between `start` and the matching list
 * close, returning the pdfmake list and the index just past it. Each item is
 * parsed recursively so nested lists and multi-block items survive.
 */
function parseList(tokens: Token[], start: number, isOrdered: boolean): { content: Content; next: number } {
  const closeType = isOrdered ? 'ordered_list_close' : 'bullet_list_close';
  const items: Content[] = [];
  let i = start + 1;

  while (i < tokens.length && tokens[i]?.type !== closeType) {
    if (tokens[i]?.type === 'list_item_open') {
      const itemLevel = tokens[i]!.level;
      let j = i + 1;
      const inner: Token[] = [];
      while (j < tokens.length && !(tokens[j]?.type === 'list_item_close' && tokens[j]!.level === itemLevel)) {
        inner.push(tokens[j]!);
        j++;
      }
      const blocks = tokensToContent(inner);
      items.push(blocks.length === 1 ? blocks[0]! : { stack: blocks });
      i = j + 1;
    } else {
      i++;
    }
  }

  return { content: isOrdered ? { ol: items } : { ul: items }, next: i + 1 };
}

/**
 * Converts a markdown-it token stream into pdfmake content blocks. Supports
 * headings, paragraphs, ordered/unordered (and nested) lists, code blocks and
 * inline emphasis/links — the subset the portal's rich-text fields produce.
 */
function tokensToContent(tokens: Token[]): Content[] {
  const content: Content[] = [];
  let i = 0;

  while (i < tokens.length) {
    const token = tokens[i]!;
    switch (token.type) {
      case 'heading_open': {
        const inline = tokens[i + 1];
        content.push({
          text: inline ? inlineToRuns(inline) : '',
          fontSize: token.tag === 'h1' ? MARKDOWN_H1_SIZE : MARKDOWN_H2_SIZE,
          bold: true,
          margin: [0, 4, 0, 2],
        });
        i += 3; // heading_open, inline, heading_close
        break;
      }
      case 'paragraph_open': {
        const inline = tokens[i + 1];
        content.push({
          text: inline ? inlineToRuns(inline) : '',
          fontSize: MARKDOWN_BODY_SIZE,
          // Hidden paragraphs come from tight list items; drop the block margin
          // so list spacing stays compact.
          margin: token.hidden ? [0, 0, 0, 0] : [0, 0, 0, 4],
        });
        i += 3; // paragraph_open, inline, paragraph_close
        break;
      }
      case 'bullet_list_open':
      case 'ordered_list_open': {
        const { content: list, next } = parseList(tokens, i, token.type === 'ordered_list_open');
        content.push(list);
        i = next;
        break;
      }
      case 'fence':
      case 'code_block': {
        content.push({
          text: token.content.replace(/\n$/, ''),
          fontSize: MARKDOWN_BODY_SIZE,
          preserveLeadingSpaces: true,
          margin: [0, 0, 0, 4],
        });
        i++;
        break;
      }
      default:
        i++;
    }
  }
  return content;
}

function markdownToContent(text: string): Content[] {
  return tokensToContent(md.parse(text, {}));
}

/**
 * A section heading on the level of "Timeline" or "Known limitations".
 * `headlineLevel` marks it for the document's `pageBreakBefore` rule so it is
 * never stranded at the bottom of a page without its content.
 */
function sectionHeading(text: string): Content {
  return { text, style: 'sectionHeading', headlineLevel: 1 };
}

/** Formats a timeline date, falling back to the "to be determined" placeholder. */
function timelineDate(date?: string): string {
  return date ? getLocaleDateString(date) : t('placeholder.toBeDetermined');
}

/**
 * Renders the release timeline as a bulleted list: preview availability, the
 * recommended test phase as a date range, and the planned production date.
 * Missing dates fall back to a placeholder so every line is always present.
 */
function renderTimeline(timeline?: ReleaseTimeline): Content {
  const line = (label: string, value: string): ContentText => ({
    text: [{ text: `${label}: `, bold: true }, value],
  });
  return {
    ul: [
      line(t('title.previewAvailableFrom'), timelineDate(timeline?.previewAvailableFrom)),
      line(
        t('title.recommendedTestPhase'),
        `${timelineDate(timeline?.recommendedTestPhaseFrom)} – ${timelineDate(timeline?.recommendedTestPhaseTo)}`,
      ),
      line(t('title.plannedProductionDeployment'), timelineDate(timeline?.plannedProductionDeployment)),
    ],
    margin: [0, 0, 0, 16],
  };
}

/**
 * Renders the change impacts as a table mirroring the in-app ChangeImpactTable:
 * feature, what changed, what to test and the testing need. Falls back to a
 * placeholder row when there are no impacts.
 */
function renderChangeImpactTable(changeImpacts: ChangeImpact[]): Content {
  const header: TableCell[] = [
    { text: t('title.feature'), style: 'tableHeader' },
    { text: t('title.whatIsChanged'), style: 'tableHeader' },
    { text: t('title.whatShouldBeTested'), style: 'tableHeader' },
    { text: t('title.testingNeed'), style: 'tableHeader' },
  ];

  const rows: TableCell[][] = (changeImpacts ?? []).map((impact) => [
    { text: impact.feature?.name ?? '' },
    { text: impact.whatIsChanged ?? '' },
    { text: impact.whatShouldBeTested ?? '' },
    { text: impact.testingNeed ? t(`testingNeeds.${impact.testingNeed.toLowerCase()}`) : '' },
  ]);

  if (rows.length === 0) {
    rows.push([{ text: t('placeholder.noChangeImpacts'), colSpan: 4, italics: true, color: '#666666' }, {}, {}, {}]);
  }

  return {
    table: { headerRows: 1, widths: ['auto', '*', '*', 'auto'], body: [header, ...rows] },
    layout: 'lightHorizontalLines',
    fontSize: 10,
    margin: [0, 0, 0, 16],
  };
}

/**
 * Renders the change-details section: its `sectionTitle` heading followed by
 * the change notes grouped by feature, one sub-heading per feature in
 * first-seen order, with featureless notes collected under "Other" at the end.
 * Each note leads with its reference — and its linked Jira service request when
 * one exists — and title, then the markdown description.
 * The section heading is owned here (rather than emitted by the caller) so it
 * can be glued to the first feature group and never stranded above a page break.
 */
function renderFeatureDetails(changeNotes: ChangeNote[], sectionTitle: string, serviceRequestKeys: Record<string, string>): Content[] {
  const groups: { name: string; notes: ChangeNote[] }[] = [];
  const groupsByFeatureId = new Map<number, { name: string; notes: ChangeNote[] }>();
  const featurelessNotes: ChangeNote[] = [];

  for (const changeNote of changeNotes ?? []) {
    if (changeNote.feature) {
      let group = groupsByFeatureId.get(changeNote.feature.id);
      if (!group) {
        group = { name: changeNote.feature.name, notes: [] };
        groupsByFeatureId.set(changeNote.feature.id, group);
        groups.push(group);
      }
      group.notes.push(changeNote);
    } else {
      featurelessNotes.push(changeNote);
    }
  }
  if (featurelessNotes.length > 0) {
    groups.push({ name: t('pdf.otherFeature'), notes: featurelessNotes });
  }

  if (groups.length === 0) {
    return [
      sectionHeading(sectionTitle),
      { text: t('placeholder.noChangeNotesAdded'), italics: true, color: '#666666', margin: [0, 0, 0, 16] },
    ];
  }

  // Each note leads with its reference — shown as a bold "Ticket ID:" label
  // followed by the value, and the linked "Service request:" key next to it when
  // the issue has one — and its title; if everything is missing it falls back to
  // a placeholder so the note still has a visible label.
  const renderNote = (changeNote: ChangeNote): Content => {
    const label: ContentText[] = [];
    if (changeNote.reference) {
      label.push({ text: `${t('pdf.ticketId')}: `, bold: true }, jiraLink(changeNote.reference));
      const serviceRequest = serviceRequestKeys[changeNote.reference];
      if (serviceRequest) {
        label.push(
          { text: ' · ' },
          { text: `${t('pdf.serviceRequest')}: `, bold: true },
          jiraLink(serviceRequest),
        );
      }
    }
    if (changeNote.title) label.push({ text: label.length ? ` ${changeNote.title}` : changeNote.title });
    if (label.length === 0) label.push({ text: t('pdf.noTitle'), italics: true });

    const note: Content[] = [{ text: label, fontSize: NOTE_HEADING_SIZE, margin: [0, 0, 0, 2] }];
    if (changeNote.description) {
      note.push({ stack: markdownToContent(changeNote.description), margin: [12, 0, 0, 8] });
    }
    return { stack: note, unbreakable: true, margin: [0, 6, 0, 0] };
  };

  // Glue each feature heading to its first change note in one unbreakable block
  // so the heading is never stranded at the foot of a page; the remaining notes
  // flow after it, each unbreakable on its own. The section heading joins the
  // first group's block for the same reason.
  return groups.flatMap((group, index) => {
    const notes = group.notes.map(renderNote);
    const block: Content[] = [{ text: group.name, style: 'featureHeading' }, notes[0]!];
    if (index === 0) block.unshift(sectionHeading(sectionTitle));
    return [
      { stack: block, unbreakable: true },
      ...notes.slice(1),
    ];
  });
}

/**
 * Builds the body shared by both PDF layouts. A draft renders the full preview
 * aimed at planning testing before deployment: an explanatory intro, the
 * release timeline, the summary, the expected-impact overview, the per-feature
 * change details, the testing responsibilities and the known limitations.
 *
 * A published release reuses the same builder but drops the preview-only
 * sections — the intro, the timeline, the expected-impact overview and the
 * testing responsibilities — leaving the summary, the change details and the
 * known limitations.
 */
function buildBody(releaseNote: ReleaseNote, changeNotes: ChangeNote[], serviceRequestKeys: Record<string, string>): Content[] {
  const isPreview = !releaseNote.published;
  return [
    ...(releaseNote.product
      ? [{ text: [{ text: `${t('title.product')}: `, bold: true }, releaseNote.product.name], margin: [0, 0, 0, 16] } as Content]
      : []),
    ...(isPreview ? [{ text: t('pdf.previewIntro'), style: 'intro' } as Content] : []),
    ...(isPreview ? [sectionHeading(t('pdf.timeline')), renderTimeline(releaseNote.releaseTimeline)] : []),
    ...(releaseNote.summary
      ? [sectionHeading(t('title.summary')), { stack: markdownToContent(releaseNote.summary), margin: [0, 0, 0, 16] } as Content]
      : []),
    ...(isPreview
      ? [
          sectionHeading(t('title.changeImpacts')),
          { text: t('pdf.previewTestingNotice'), margin: [0, 0, 0, 8] } as Content,
          renderChangeImpactTable(releaseNote.changeImpacts),
        ]
      : []),
    ...renderFeatureDetails(changeNotes, t('pdf.featureDetails'), serviceRequestKeys),
    ...(isPreview
      ? [sectionHeading(t('pdf.testingResponsibility')), ...markdownToContent(t('pdf.testingResponsibilityBody'))]
      : []),
    sectionHeading(t('title.knownLimitations')),
    renderKnownLimitations(releaseNote.knownLimitations),
  ];
}

/** Renders the release note's known limitations as a bulleted list. */
function renderKnownLimitations(knownLimitations: string[]): Content {
  if (!knownLimitations?.length) {
    return { text: t('placeholder.noKnownLimitations'), italics: true, color: '#666666', fontSize: MARKDOWN_BODY_SIZE };
  }
  return { ul: knownLimitations.map((limitation) => ({ text: limitation })), fontSize: MARKDOWN_BODY_SIZE };
}

/**
 * Exports a release note to a downloaded PDF file.
 *
 * The layout depends on the release note's publication state: a published note
 * produces the customer-facing release document, while a draft produces a
 * preview aimed at planning testing before deployment. The preview carries
 * extra sections (intro, timeline, expected-impact overview and testing
 * responsibilities) and labels its title accordingly; both are authored from
 * the supplied change notes, which the caller has already filtered and
 * optionally translated. The `serviceRequestKeys` map links each change note's
 * reference to its Jira service-request key and is rendered next to the
 * reference on the notes that have one.
 *
 * This PDF is customer-facing, so it deliberately omits the change notes'
 * developer notes and upgrade requirements, which are internal-only.
 */
export async function exportToPdf(releaseNote: ReleaseNote, changeNotes: ChangeNote[], serviceRequestKeys: Record<string, string> = {}) {
  const now = new Date();
  const generatedDate = [
    String(now.getDate()).padStart(2, '0'),
    String(now.getMonth() + 1).padStart(2, '0'),
    now.getFullYear(),
  ].join('.');

  // A preview is labelled as such in the title. The label is intentionally not
  // translated so the document reads the same across locales.
  const title = releaseNote.published ? releaseNote.tag : `Release preview: ${releaseNote.tag}`;
  const generated = { text: `${t('pdf.generated')}: ${generatedDate}`, style: 'generated', width: 'auto', noWrap: true };

  const content: Content[] = [
    { svg: blackLogo, width: 160, margin: [0, 0, 0, 24], alignment: 'right' },
  ];

  if (releaseNote.published) {
    // The release title and the generated date share the top row.
    content.push({ columns: [{ text: title, style: 'tag' }, generated] });
  } else {
    // The preview title spans the row on its own; the generated date sits below.
    content.push({ text: title, style: 'tag' }, generated);
  }
  content.push(...buildBody(releaseNote, changeNotes, serviceRequestKeys));

  const documentDefinition: TDocumentDefinitions = {
    info: { title: releaseNote.tag },
    content,
    // Keep every heading with its content: a heading marked with headlineLevel
    // moves to the next page when nothing — or only further headings — would
    // follow it on the current page.
    pageBreakBefore: (currentNode, nodeQueries) =>
      currentNode.headlineLevel === 1 &&
      nodeQueries.getFollowingNodesOnPage().every((node) => node.headlineLevel === 1),
    styles: {
      tag: { fontSize: 24, bold: true, margin: [0, 0, 0, 8] },
      generated: { fontSize: 10, color: '#666666', alignment: 'left', margin: [0, 0, 0, 8] },
      intro: { fontSize: 11, italics: true, color: '#666666', margin: [0, 0, 0, 16] },
      sectionHeading: { fontSize: 16, bold: true, margin: [0, 16, 0, 8] },
      featureHeading: { fontSize: 14, bold: true, margin: [0, 10, 0, 4] },
      tableHeader: { fontSize: 10, bold: true, fillColor: '#f0f0f0' },
    },
    defaultStyle: { font: 'Roboto', lineHeight: 1.3 },
  };

  pdfMake.createPdf(documentDefinition).download(`${releaseNote.published ? 'Release' : 'Preview'} ${releaseNote.tag}.pdf`);
}
