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
import Pagination from '../Pagination.vue';

describe('Pagination.vue', () => {
    it('renders correct stats text for plural and singular cases', () => {
        const wrapper = mount(Pagination, {
            props: {
                modelValue: 0,
                totalPages: 3,
                totalElements: 25,
                itemName: 'document',
                pluralItemName: 'documents'
            }
        });
        expect(wrapper.text()).toContain('25 total documents');

        const wrapperSingular = mount(Pagination, {
            props: {
                modelValue: 0,
                totalPages: 1,
                totalElements: 1,
                itemName: 'document',
                pluralItemName: 'documents'
            }
        });
        expect(wrapperSingular.text()).toContain('1 total document');
    });

    it('disables previous button on first page and next button on last page', () => {
        const wrapperFirst = mount(Pagination, {
            props: {
                modelValue: 0,
                totalPages: 5,
                totalElements: 50
            }
        });
        const prevBtn = wrapperFirst
            .findAll('button')
            .find((b) => b.text() === 'Previous')!;
        const nextBtn = wrapperFirst
            .findAll('button')
            .find((b) => b.text() === 'Next')!;

        expect(prevBtn.element.disabled).toBe(true);
        expect(nextBtn.element.disabled).toBe(false);

        const wrapperLast = mount(Pagination, {
            props: {
                modelValue: 4,
                totalPages: 5,
                totalElements: 50
            }
        });
        const prevBtnLast = wrapperLast
            .findAll('button')
            .find((b) => b.text() === 'Previous')!;
        const nextBtnLast = wrapperLast
            .findAll('button')
            .find((b) => b.text() === 'Next')!;

        expect(prevBtnLast.element.disabled).toBe(false);
        expect(nextBtnLast.element.disabled).toBe(true);
    });

    it('emits update:modelValue event when a page number or next/previous button is clicked', async () => {
        const createWrapper = () =>
            mount(Pagination, {
                props: {
                    modelValue: 1,
                    totalPages: 5,
                    totalElements: 50
                }
            });

        const wrapper1 = createWrapper();
        const pageBtn = wrapper1
            .findAll('button')
            .find((b) => b.text() === '3')!;
        await pageBtn.trigger('click');
        expect(wrapper1.emitted('update:modelValue')?.[0]).toEqual([2]);

        const wrapper2 = createWrapper();
        const prevBtn = wrapper2
            .findAll('button')
            .find((b) => b.text() === 'Previous')!;
        await prevBtn.trigger('click');
        expect(wrapper2.emitted('update:modelValue')?.[0]).toEqual([0]);

        const wrapper3 = createWrapper();
        const nextBtn = wrapper3
            .findAll('button')
            .find((b) => b.text() === 'Next')!;
        await nextBtn.trigger('click');
        expect(wrapper3.emitted('update:modelValue')?.[0]).toEqual([2]);
    });
});
