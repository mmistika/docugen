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

import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import ImagePicker from '../ImagePicker.vue';

describe('ImagePicker.vue', () => {
    it('renders with initials when value is null', () => {
        const wrapper = mount(ImagePicker, {
            props: {
                modelValue: null,
                initials: 'JD'
            }
        });

        expect(wrapper.text()).toContain('JD');
        expect(wrapper.find('img').exists()).toBe(false);
    });

    it('renders avatar image when modelValue is present', () => {
        const wrapper = mount(ImagePicker, {
            props: {
                modelValue: 'data:image/png;base64,mocked-base64',
                initials: 'JD'
            }
        });

        expect(wrapper.find('img').exists()).toBe(true);
        expect(wrapper.find('img').attributes('src')).toBe(
            'data:image/png;base64,mocked-base64'
        );
    });

    it('triggers file input click when upload button is clicked', async () => {
        const wrapper = mount(ImagePicker, {
            props: {
                modelValue: null,
                initials: 'JD'
            }
        });

        const fileInput = wrapper.find('input[type="file"]')
            .element as HTMLInputElement;
        const spy = vi.spyOn(fileInput, 'click');

        const uploadButton = wrapper.find('button');
        await uploadButton.trigger('click');

        expect(spy).toHaveBeenCalled();
    });

    it('emits update:modelValue null when remove is clicked', async () => {
        const wrapper = mount(ImagePicker, {
            props: {
                modelValue: 'data:image/png;base64,mocked-base64',
                initials: 'JD'
            }
        });

        const buttons = wrapper.findAll('button');
        const removeButton = buttons.find((b) => b.text().includes('Remove'));
        expect(removeButton).toBeDefined();

        await removeButton!.trigger('click');

        expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([null]);
    });
});
