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
import PermissionSelector from '../PermissionSelector.vue';
import type { PermissionDTO } from '@/types/rbac.ts';

describe('PermissionSelector.vue', () => {
    const allPermissions: PermissionDTO[] = [
        { id: 1, name: 'document:create' },
        { id: 2, name: 'document:view' },
        { id: 3, name: 'user:invite' },
        { id: 4, name: 'other_perm' }
    ];

    it('groups permissions correctly', () => {
        const wrapper = mount(PermissionSelector, {
            props: {
                allPermissions,
                modelValue: new Set<string>()
            }
        });

        const headers = wrapper.findAll('button span.font-semibold');
        expect(headers).toHaveLength(3); // document, user, other
        expect(headers[0].text()).toBe('Document');
        expect(headers[1].text()).toBe('User');
        expect(headers[2].text()).toBe('Other');
    });

    it('toggles permission selection when a label is clicked', async () => {
        const wrapper = mount(PermissionSelector, {
            props: {
                allPermissions,
                modelValue: new Set<string>(['document:view']),
                'onUpdate:modelValue': (val: Set<string>) => {
                    wrapper.setProps({ modelValue: val });
                }
            }
        });

        const labels = wrapper.findAll('label');
        const createLabel = labels.find((l) => l.text().includes('Create'))!;

        await createLabel.find('div').trigger('click');

        const emitted = wrapper.emitted('update:modelValue');
        expect(emitted).toBeTruthy();
        const nextSet = emitted![0][0] as Set<string>;
        expect(nextSet.has('document:create')).toBe(true);
        expect(nextSet.has('document:view')).toBe(true);
    });

    it('toggles whole group when group header button is clicked', async () => {
        const wrapper = mount(PermissionSelector, {
            props: {
                allPermissions,
                modelValue: new Set<string>(['document:view']),
                'onUpdate:modelValue': (val: Set<string>) => {
                    wrapper.setProps({ modelValue: val });
                }
            }
        });

        const groupButton = wrapper.findAll('button')[0];
        await groupButton.trigger('click');

        const emitted = wrapper.emitted('update:modelValue');
        expect(emitted).toBeTruthy();
        const nextSet = emitted![0][0] as Set<string>;
        expect(nextSet.has('document:create')).toBe(true);
        expect(nextSet.has('document:view')).toBe(true);

        // If all selected, clicking deselects all
        const wrapperAllSelected = mount(PermissionSelector, {
            props: {
                allPermissions,
                modelValue: new Set<string>([
                    'document:create',
                    'document:view'
                ])
            }
        });
        const groupButtonAll = wrapperAllSelected.findAll('button')[0];
        await groupButtonAll.trigger('click');

        const emittedAll = wrapperAllSelected.emitted('update:modelValue');
        expect(emittedAll).toBeTruthy();
        const nextSetAll = emittedAll![0][0] as Set<string>;
        expect(nextSetAll.has('document:create')).toBe(false);
        expect(nextSetAll.has('document:view')).toBe(false);
    });
});
