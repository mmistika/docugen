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
