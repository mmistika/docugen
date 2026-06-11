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

import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';
import { useEditorFormatting } from '../useEditorFormatting';

describe('useEditorFormatting Composable', () => {
    let mockEditor: any;
    let runMock: any;
    let chainMock: any;
    let focusMock: any;

    beforeEach(() => {
        runMock = vi.fn();
        focusMock = {
            unsetFontFamily: vi.fn().mockReturnThis(),
            setFontFamily: vi.fn().mockReturnThis(),
            unsetFontSize: vi.fn().mockReturnThis(),
            setFontSize: vi.fn().mockReturnThis(),
            setColor: vi.fn().mockReturnThis(),
            unsetColor: vi.fn().mockReturnThis(),
            toggleHighlight: vi.fn().mockReturnThis(),
            unsetHighlight: vi.fn().mockReturnThis(),
            setParagraph: vi.fn().mockReturnThis(),
            toggleHeading: vi.fn().mockReturnThis(),
            run: runMock
        };
        chainMock = {
            focus: vi.fn().mockReturnValue(focusMock)
        };
        mockEditor = {
            getAttributes: vi.fn(),
            isActive: vi.fn(),
            chain: vi.fn().mockReturnValue(chainMock)
        };
    });

    it('should resolve editor from ref, function, or raw object', () => {
        // Test ref resolution
        const editorRef = ref(mockEditor);
        const { getActiveFontFamily } = useEditorFormatting(editorRef);
        mockEditor.getAttributes.mockReturnValue({ fontFamily: 'Arial' });
        expect(getActiveFontFamily()).toBe('Arial');

        // Test function resolution
        const editorFn = () => mockEditor;
        const { getActiveFontSize } = useEditorFormatting(editorFn);
        mockEditor.getAttributes.mockReturnValue({ fontSize: '18px' });
        expect(getActiveFontSize()).toBe('18px');

        // Test direct object resolution
        const { getActiveColor } = useEditorFormatting(mockEditor);
        mockEditor.getAttributes.mockReturnValue({ color: '#ff0000' });
        expect(getActiveColor()).toBe('#ff0000');
    });

    it('should fallback to default formatting values when attributes are missing', () => {
        const {
            getActiveFontFamily,
            getActiveFontSize,
            getActiveColor,
            getActiveHighlight
        } = useEditorFormatting(mockEditor);
        mockEditor.getAttributes.mockReturnValue({});

        expect(getActiveFontFamily()).toBe('Inter');
        expect(getActiveFontSize()).toBe('14px');
        expect(getActiveColor()).toBe('#1f2937');
        expect(getActiveHighlight()).toBe('transparent');
    });

    it('should set font family or unset it if default is selected', () => {
        const { setFontFamily } = useEditorFormatting(mockEditor);

        // Custom font
        setFontFamily('Roboto');
        expect(mockEditor.chain).toHaveBeenCalled();
        expect(focusMock.setFontFamily).toHaveBeenCalledWith('Roboto');
        expect(runMock).toHaveBeenCalled();

        vi.clearAllMocks();

        // Default font (Inter) should unset
        setFontFamily('Inter');
        expect(mockEditor.chain).toHaveBeenCalled();
        expect(focusMock.unsetFontFamily).toHaveBeenCalled();
        expect(runMock).toHaveBeenCalled();
    });

    it('should set font size or unset it if default is selected', () => {
        const { setFontSize } = useEditorFormatting(mockEditor);

        // Custom size
        setFontSize('16px');
        expect(mockEditor.chain).toHaveBeenCalled();
        expect(focusMock.setFontSize).toHaveBeenCalledWith('16px');
        expect(runMock).toHaveBeenCalled();

        vi.clearAllMocks();

        // Default size (14px) should unset
        setFontSize('14px');
        expect(mockEditor.chain).toHaveBeenCalled();
        expect(focusMock.unsetFontSize).toHaveBeenCalled();
        expect(runMock).toHaveBeenCalled();
    });

    it('should set and reset text color', () => {
        const { setTextColor, resetTextColor } =
            useEditorFormatting(mockEditor);

        setTextColor('#00ff00');
        expect(focusMock.setColor).toHaveBeenCalledWith('#00ff00');
        expect(runMock).toHaveBeenCalled();

        vi.clearAllMocks();

        resetTextColor();
        expect(focusMock.unsetColor).toHaveBeenCalled();
        expect(runMock).toHaveBeenCalled();
    });

    it('should set and reset highlight color', () => {
        const { setHighlightColor, resetHighlightColor } =
            useEditorFormatting(mockEditor);

        setHighlightColor('yellow');
        expect(focusMock.toggleHighlight).toHaveBeenCalledWith({
            color: 'yellow'
        });
        expect(runMock).toHaveBeenCalled();

        vi.clearAllMocks();

        resetHighlightColor();
        expect(focusMock.unsetHighlight).toHaveBeenCalled();
        expect(runMock).toHaveBeenCalled();
    });

    it('should determine active heading level or paragraph', () => {
        const { getActiveHeading } = useEditorFormatting(mockEditor);

        mockEditor.isActive.mockImplementation((name: string, attrs?: any) => {
            return name === 'heading' && attrs?.level === 1;
        });
        expect(getActiveHeading()).toBe('1');

        mockEditor.isActive.mockImplementation((name: string, attrs?: any) => {
            return name === 'heading' && attrs?.level === 3;
        });
        expect(getActiveHeading()).toBe('3');

        mockEditor.isActive.mockReturnValue(false);
        expect(getActiveHeading()).toBe('paragraph');
    });

    it('should set heading levels or paragraph', () => {
        const { setHeading } = useEditorFormatting(mockEditor);

        setHeading('2');
        expect(focusMock.toggleHeading).toHaveBeenCalledWith({ level: 2 });
        expect(runMock).toHaveBeenCalled();

        vi.clearAllMocks();

        setHeading('paragraph');
        expect(focusMock.setParagraph).toHaveBeenCalled();
        expect(runMock).toHaveBeenCalled();
    });
});
