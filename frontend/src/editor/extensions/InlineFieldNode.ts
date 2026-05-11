import { mergeAttributes, Node } from '@tiptap/core';

export const InlineFieldNode = Node.create({
    name: 'inlineField',
    group: 'inline',
    inline: true,
    selectable: true,
    atom: true,

    addAttributes() {
        return {
            id: { default: null },
            name: { default: 'new_field' },
            type: { default: 'text' },
            required: { default: false }
        };
    },

    parseHTML() {
        return [{ tag: 'span[data-type="inline-field"]' }];
    },

    renderHTML({ HTMLAttributes }) {
        return [
            'span',
            mergeAttributes(HTMLAttributes, {
                'data-type': 'inline-field',
                class:
                    'inline-flex items-center px-1.5 py-0.5 rounded text-xs font-mono font-medium ' +
                    'bg-blue-100 text-blue-800 border border-blue-200 cursor-pointer mx-1 select-all ' +
                    'transition-colors hover:bg-blue-200'
            }),
            `{${HTMLAttributes.name}}`
        ];
    }
});
