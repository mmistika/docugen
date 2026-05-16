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
