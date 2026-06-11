/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
