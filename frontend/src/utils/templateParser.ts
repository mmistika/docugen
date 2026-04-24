export function buildInteractiveContent(html: string): string {
    const parser = new DOMParser()
    const doc    = parser.parseFromString(html, 'text/html')

    doc.querySelectorAll<HTMLElement>('span[data-type="inline-field"]').forEach((span) => {
        const name      = span.getAttribute('name')    ?? ''
        const fieldType = span.getAttribute('type')    ?? 'text'
        const required  = span.getAttribute('required') === 'true'

        const input = doc.createElement('input')
        input.setAttribute('data-field-name', name)
        input.setAttribute('type', fieldType === 'number' ? 'number' : 'text')
        input.setAttribute('placeholder', `{${name}}`)
        input.setAttribute('aria-label', name)
        input.setAttribute('size', String(Math.max(name.length + 2, 4)))
        if (required) input.setAttribute('required', 'true')
        input.className = 'inline-doc-input'

        span.replaceWith(input)
    })

    return doc.body.innerHTML
}