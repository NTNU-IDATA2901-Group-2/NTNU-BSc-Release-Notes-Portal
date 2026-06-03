import solwrLogo from '@/assets/solwr_logo.svg?raw';
import md from './markdown-it';
import { i18n } from './i18n';
import type { ChangeNote } from './types';
import pdfMake from "pdfmake/build/pdfmake";
import vfs from "pdfmake/build/vfs_fonts";
import type { Content, ContentText, TDocumentDefinitions } from "pdfmake/interfaces";

pdfMake.addVirtualFileSystem(vfs);

// The logo's colors are baked into the SVG's `fill` attributes, so pdfmake's
// `color` option has no effect. Recolor every painted fill to black instead.
const blackLogo = solwrLogo.replace(/fill="(?!none")[^"]*"/g, 'fill="black"');

type Token = ReturnType<typeof md.parse>[number];

// Font sizes for headings authored inside markdown content. `#` (h1) renders
// larger than `##`, and every level below `##` collapses to the `##` size.
// Both stay below the change note title (16) so a `#` in the markdown can
// never outrank the structure around it.
const MARKDOWN_H1_SIZE = 13;
const MARKDOWN_H2_SIZE = 11;
const MARKDOWN_BODY_SIZE = 11;

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
          ...(link ? { link, color: '#0b5cff', decoration: 'underline' } : {}),
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
 * Exports a release note to a downloaded PDF file.
 *
 * The document leads with the release note's tag and summary, followed by the
 * change notes grouped together by feature. Groups appear in the order their
 * feature is first encountered, and change notes without a feature are listed
 * last. Within each feature the notes are further grouped by customer, with
 * customerless notes at the bottom. Each change note leads with a heading
 * line — "Title (reference)"
 * followed by the feature name when present — then the customer on its own line
 * and the description beneath. Change notes without a title or reference fall
 * back to localized placeholders. The summary and description are authored in
 * markdown and rendered with matching headings, lists and emphasis.
 *
 * This PDF is customer-facing, so it deliberately omits the change notes'
 * developer notes and upgrade requirements, which are internal-only.
 */
export async function exportToPdf(releaseNoteTag: string, releaseNoteSummary: string, changeNotes: ChangeNote[]) {
  const now = new Date();
  const generatedDate = [
    String(now.getDate()).padStart(2, '0'),
    String(now.getMonth() + 1).padStart(2, '0'),
    now.getFullYear(),
  ].join('.');

  const content: Content[] = [
    { svg: blackLogo, width: 160, margin: [0, 0, 0, 24], alignment: 'right' },
    {
      columns: [
        { text: releaseNoteTag, style: 'tag' },
        { text: `${i18n.global.t('pdf.generated')}: ${generatedDate}`, style: 'generated' },
      ],
    },
  ];

  if (releaseNoteSummary) {
    content.push({ stack: markdownToContent(releaseNoteSummary), margin: [0, 0, 0, 24] });
  }

  // Group change notes by feature, preserving the order in which features are
  // first seen. Notes without a feature are collected separately and rendered
  // last, after all feature groups. The feature is shown on each note rather
  // than as a separate group heading.
  const featureGroups = new Map<number, ChangeNote[]>();
  const featurelessNotes: ChangeNote[] = [];
  for (const changeNote of changeNotes ?? []) {
    if (changeNote.feature) {
      const group = featureGroups.get(changeNote.feature.id);
      if (group) {
        group.push(changeNote);
      } else {
        featureGroups.set(changeNote.feature.id, [changeNote]);
      }
    } else {
      featurelessNotes.push(changeNote);
    }
  }

  // Within a feature, order notes so those sharing a customer are contiguous
  // (in first-seen order), with customerless notes placed at the bottom.
  const orderByCustomer = (notes: ChangeNote[]): ChangeNote[] => {
    const customerGroups = new Map<number, ChangeNote[]>();
    const customerlessNotes: ChangeNote[] = [];
    for (const note of notes) {
      if (note.customer) {
        const group = customerGroups.get(note.customer.id);
        if (group) {
          group.push(note);
        } else {
          customerGroups.set(note.customer.id, [note]);
        }
      } else {
        customerlessNotes.push(note);
      }
    }
    return [...[...customerGroups.values()].flat(), ...customerlessNotes];
  };

  const renderChangeNote = (changeNote: ChangeNote): Content => {
    const titleText = changeNote.title || i18n.global.t('pdf.noTitle');
    const referenceText = changeNote.reference || i18n.global.t('pdf.noReference');

    // The heading line reads "Title (reference)", with the feature name
    // appended when the note has one.
    const heading: ContentText[] = [
      { text: titleText, style: 'title' },
      { text: ` (${referenceText})`, style: 'reference' },
    ];
    if (changeNote.feature) {
      heading.push({ text: ` ${changeNote.feature.name}`, style: 'feature' });
    }

    // Group each change note into one unbreakable block so a note is never
    // split across a page boundary: the heading line, an optional customer line
    // and the markdown description beneath.
    const note: Content[] = [{ text: heading, margin: [0, 0, 0, 4] }];
    if (changeNote.customer) {
      note.push({ text: changeNote.customer.name, style: 'customer' });
    }
    if (changeNote.description) {
      note.push({ stack: markdownToContent(changeNote.description), margin: [12, 4, 0, 8] });
    }
    return { stack: note, unbreakable: true, margin: [0, 16, 0, 0] };
  };

  for (const group of featureGroups.values()) {
    for (const changeNote of orderByCustomer(group)) {
      content.push(renderChangeNote(changeNote));
    }
  }
  for (const changeNote of orderByCustomer(featurelessNotes)) {
    content.push(renderChangeNote(changeNote));
  }

  const documentDefinition: TDocumentDefinitions = {
    info: { title: releaseNoteTag },
    content,
    styles: {
      tag: { fontSize: 24, bold: true, margin: [0, 0, 0, 8] },
      generated: { fontSize: 10, color: '#666666', alignment: 'right', margin: [0, 8, 0, 0] },
      title: { fontSize: 16, bold: true },
      reference: { fontSize: 12, bold: true },
      customer: { fontSize: 12, italics: true, color: '#666666' },
      feature: { fontSize: 12, bold: true, color: '#666666' },
    },
    defaultStyle: { font: 'Roboto' },
  };

  pdfMake.createPdf(documentDefinition).download(`${releaseNoteTag}.pdf`);
}
