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
import FieldSidebar from '../FieldSidebar.vue';
import type { Field } from '@/types/field';

describe('FieldSidebar.vue', () => {
    const globalFields: Field[] = [
        { id: 'g1', name: 'Global1', type: 'text', required: true }
    ];
    const inlineFields: Field[] = [
        { id: 'i1', name: 'Inline1', type: 'number', required: false }
    ];

    it('renders global and inline field sections', () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null
            }
        });

        expect(wrapper.text()).toContain('Global Fields');
        expect(wrapper.text()).toContain('Inline Embeds');
        expect(wrapper.text()).toContain('Global1');
        expect(wrapper.text()).toContain('Inline1');
    });

    it('emits add-global when a global field button is clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields: [],
                inlineFields: [],
                selectedFieldId: null
            }
        });

        const globalBtn = wrapper.findAll('.flex-col').at(0)?.find('button');
        await globalBtn?.trigger('click');

        expect(wrapper.emitted('add-global')).toBeTruthy();
    });

    it('emits select-field when field row is clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null
            }
        });

        const fieldBtn = wrapper
            .findAll('button')
            .find((b) => b.text().includes('Global1'))!;
        await fieldBtn.trigger('click');

        expect(wrapper.emitted('select-field')?.[0]).toEqual(['g1']);
    });

    it('toggles visibility of global and inline fields when header buttons are clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null
            }
        });

        expect(wrapper.text()).toContain('Global1');
        expect(wrapper.text()).toContain('Inline1');

        const toggleButtons = wrapper
            .findAll('button')
            .filter(
                (b) =>
                    b.text().includes('Fields') ||
                    b.text().includes('Inline Fields')
            );

        await toggleButtons[0].trigger('click');
        expect(wrapper.text()).not.toContain('Global1');
        expect(wrapper.text()).toContain('Inline1');

        await toggleButtons[1].trigger('click');
        expect(wrapper.text()).not.toContain('Global1');
        expect(wrapper.text()).not.toContain('Inline1');
    });
});
