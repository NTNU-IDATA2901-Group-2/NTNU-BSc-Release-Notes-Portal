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
// Both stay below the change note reference (16) so a `#` in the markdown can
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
 * The document leads with the release note's tag and summary, followed by each
 * change note listed with its reference and description. When a change note is
 * tied to a customer, the customer name is shown next to the reference. The
 * summary and description are authored in markdown and rendered with matching
 * headings, lists and emphasis.
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

  for (const changeNote of changeNotes ?? []) {
    const reference: Content[] = [{ text: changeNote.reference, bold: true }];
    if (changeNote.customer) {
      reference.push({ text: `  ${changeNote.customer.name}`, style: 'customer' });
    }

    // Group each change note's reference and description into one unbreakable
    // block so a note is never split across a page boundary.
    const note: Content[] = [{ text: reference, style: 'reference' }];
    if (changeNote.description) {
      note.push({ stack: markdownToContent(changeNote.description), margin: [12, 0, 0, 8] });
    }
    content.push({ stack: note, unbreakable: true });
  }

  const documentDefinition: TDocumentDefinitions = {
    info: { title: releaseNoteTag },
    content,
    styles: {
      tag: { fontSize: 24, bold: true, margin: [0, 0, 0, 8] },
      generated: { fontSize: 10, color: '#666666', alignment: 'right', margin: [0, 8, 0, 0] },
      reference: { fontSize: 16, margin: [0, 16, 0, 4] },
      customer: { fontSize: 12, italics: true, color: '#666666' },
    },
    defaultStyle: { font: 'Roboto' },
  };

  pdfMake.createPdf(documentDefinition).download(`${releaseNoteTag}.pdf`);
}
