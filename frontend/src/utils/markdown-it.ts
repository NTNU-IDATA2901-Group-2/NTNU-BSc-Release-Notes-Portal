import markdownit from 'markdown-it'

const md = markdownit()

// default to small text for markdown content
md.renderer.rules.paragraph_open = (tokens, id) =>  {
  const token = tokens[id]
  if (token !== undefined && token.level > 0) {
    return '<span class="text-sm">'; // span is needed to avoid newline inside lists
  }
  return '<p class="text-sm">';
}

// style list items for markdown
md.renderer.rules.bullet_list_open = () => '<ul class="list-disc ml-4 ">'
md.renderer.rules.ordered_list_open = () => '<ol class="text-sm list-decimal ml-4">'

md.renderer.rules.heading_open = (tokens, id) => {
  const level = tokens[id]?.tag

  if (level === undefined) {
    console.error('Markdown-it token tag undefined for id:', id);
    return '<h3 class="text-sm font-bold">';
  }

  if (level === 'h1') {
    return `<${level} class="text-lg font-bold">`
  } else if (level === 'h2') {
    return `<${level} class="text-base font-bold">`
  } else {
    return `<h3 class="text-sm font-bold">`
  }
}

export default md