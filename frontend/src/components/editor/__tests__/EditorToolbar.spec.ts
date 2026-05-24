import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import EditorToolbar from '../EditorToolbar.vue';

describe('EditorToolbar.vue', () => {
    it('renders toolbar components and passes the editor prop', () => {
        const mockEditor = { state: {}, commands: {} };
        const wrapper = mount(EditorToolbar, {
            props: {
                editor: mockEditor
            },
            global: {
                stubs: {
                    HistoryButtons: {
                        template: '<div class="history-mock">History</div>'
                    },
                    FontPresets: {
                        template: '<div class="font-mock">Font</div>'
                    },
                    TypographyPresets: {
                        template:
                            '<div class="typography-mock">Typography</div>'
                    },
                    FormatButtons: {
                        template: '<div class="format-mock">Format</div>'
                    },
                    AlignmentButtons: {
                        template: '<div class="align-mock">Align</div>'
                    },
                    ListButtons: {
                        template: '<div class="list-mock">List</div>'
                    },
                    ExtraButtons: {
                        template: '<div class="extra-mock">Extra</div>'
                    }
                }
            }
        });

        expect(wrapper.find('.history-mock').exists()).toBe(true);
        expect(wrapper.find('.font-mock').exists()).toBe(true);
        expect(wrapper.find('.typography-mock').exists()).toBe(true);
        expect(wrapper.find('.format-mock').exists()).toBe(true);
    });
});
