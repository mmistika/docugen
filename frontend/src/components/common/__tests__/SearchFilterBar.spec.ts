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
import SearchFilterBar from '../SearchFilterBar.vue';

describe('SearchFilterBar.vue', () => {
    it('renders search input with custom placeholder', () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: '',
                placeholder: 'Find files...'
            }
        });
        const input = wrapper.find('input');
        expect(input.attributes('placeholder')).toBe('Find files...');
    });

    it('emits update:modelValue when text is typed', async () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: ''
            }
        });
        const input = wrapper.find('input');
        await input.setValue('document');
        expect(wrapper.emitted('update:modelValue')?.[0][0]).toBe('document');
    });

    it('shows clear button and clears search when clicked', async () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: 'hello'
            }
        });

        const clearBtn = wrapper.find('button[title="Clear search"]');
        expect(clearBtn.exists()).toBe(true);

        await clearBtn.trigger('click');
        expect(wrapper.emitted('update:modelValue')?.[0][0]).toBe('');
    });

    it('renders custom filters slot', () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: ''
            },
            slots: {
                filters: '<button id="filter-btn">Filter</button>'
            }
        });
        expect(wrapper.find('#filter-btn').exists()).toBe(true);
    });
});
