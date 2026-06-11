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
import FieldTypeButton from '../FieldTypeButton.vue';
import type { FieldTypeDef } from '@/types/field';

describe('FieldTypeButton.vue', () => {
    const def: FieldTypeDef = {
        type: 'text',
        label: 'Text Box',
        icon: 'Type',
        defaults: {},
        properties: []
    };

    it('renders the label correctly', () => {
        const wrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });
        expect(wrapper.text()).toContain('Text Box');
    });

    it('applies correct CSS classes based on the variant prop', () => {
        const globalWrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });
        expect(globalWrapper.classes()).toContain('bg-gray-50');

        const inlineWrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'inline'
            }
        });
        expect(inlineWrapper.classes()).toContain('bg-blue-50');
    });

    it('emits click event with correct field type', async () => {
        const wrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });

        await wrapper.trigger('click');
        expect(wrapper.emitted('click')?.[0]).toEqual(['text']);
    });
});
