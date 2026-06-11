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

import { unref } from 'vue';

export function useEditorFormatting(editor: any) {
    const getEditor = () => {
        const val = unref(editor);
        if (typeof val === 'function') {
            return val();
        }
        return val;
    };

    const getActiveFontFamily = () => {
        const ed = getEditor();
        return ed?.getAttributes('textStyle').fontFamily || 'Inter';
    };

    const setFontFamily = (font: string) => {
        const ed = getEditor();
        if (!ed) return;
        if (font === 'Inter') {
            ed.chain().focus().unsetFontFamily().run();
        } else {
            ed.chain().focus().setFontFamily(font).run();
        }
    };

    const getActiveFontSize = () => {
        const ed = getEditor();
        return ed?.getAttributes('textStyle').fontSize || '14px';
    };

    const setFontSize = (size: string) => {
        const ed = getEditor();
        if (!ed) return;
        if (size === '14px') {
            ed.chain().focus().unsetFontSize().run();
        } else {
            ed.chain().focus().setFontSize(size).run();
        }
    };

    const getActiveColor = () => {
        const ed = getEditor();
        return ed?.getAttributes('textStyle').color || '#1f2937';
    };

    const setTextColor = (color: string) => {
        const ed = getEditor();
        ed?.chain().focus().setColor(color).run();
    };

    const resetTextColor = () => {
        const ed = getEditor();
        ed?.chain().focus().unsetColor().run();
    };

    const getActiveHighlight = () => {
        const ed = getEditor();
        return ed?.getAttributes('highlight').color || 'transparent';
    };

    const setHighlightColor = (color: string) => {
        const ed = getEditor();
        ed?.chain().focus().toggleHighlight({ color }).run();
    };

    const resetHighlightColor = () => {
        const ed = getEditor();
        ed?.chain().focus().unsetHighlight().run();
    };

    const getActiveHeading = () => {
        const ed = getEditor();
        if (!ed) return 'paragraph';
        if (ed.isActive('heading', { level: 1 })) return '1';
        if (ed.isActive('heading', { level: 2 })) return '2';
        if (ed.isActive('heading', { level: 3 })) return '3';
        return 'paragraph';
    };

    const setHeading = (value: string) => {
        const ed = getEditor();
        if (!ed) return;
        if (value === 'paragraph') {
            ed.chain().focus().setParagraph().run();
        } else {
            ed.chain()
                .focus()
                .toggleHeading({ level: Number(value) as 1 | 2 | 3 })
                .run();
        }
    };

    return {
        getActiveFontFamily,
        setFontFamily,
        getActiveFontSize,
        setFontSize,
        getActiveColor,
        setTextColor,
        resetTextColor,
        getActiveHighlight,
        setHighlightColor,
        resetHighlightColor,
        getActiveHeading,
        setHeading
    };
}
