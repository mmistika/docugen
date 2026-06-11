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
import RoleModal from '../RoleModal.vue';
import type { PermissionDTO, RoleDTO } from '@/types/rbac';

describe('RoleModal.vue', () => {
    const allPermissions: PermissionDTO[] = [
        { id: 1, name: 'document:create' },
        { id: 2, name: 'document:view' }
    ];

    it('renders "Create Role" in create mode', () => {
        const wrapper = mount(RoleModal, {
            props: {
                show: true,
                role: null,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true, PermissionSelector: true }
            }
        });

        expect(wrapper.text()).toContain('Create Role');
        const input = wrapper.find('input');
        expect(input.attributes()).not.toHaveProperty('disabled');
    });

    it('renders "Edit Role" in edit mode and disables name input', () => {
        const role: RoleDTO = {
            name: 'EDITOR',
            permissions: ['document:create']
        };
        const wrapper = mount(RoleModal, {
            props: {
                show: true,
                role,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true, PermissionSelector: true }
            }
        });

        expect(wrapper.text()).toContain('Edit Role');
        const input = wrapper.find('input');
        expect(input.element.value).toBe('EDITOR');
        expect(input.attributes()).toHaveProperty('disabled');
    });

    it('validates empty name or no permissions in create mode', async () => {
        const wrapper = mount(RoleModal, {
            props: {
                show: true,
                role: null,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true, PermissionSelector: true }
            }
        });

        const saveButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Create Role'))[0];
        await saveButton.trigger('click');

        expect(wrapper.text()).toContain('Role name is required.');
        expect(wrapper.emitted('save')).toBeFalsy();
    });

    it('emits save event with input values on valid submit', async () => {
        const wrapper = mount(RoleModal, {
            props: {
                show: true,
                role: null,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const input = wrapper.find('input');
        await input.setValue('PUBLISHER');

        const checkbox = wrapper.find('input[type="checkbox"]');
        // @ts-expect-error TS2341: Property setChecked is private and only accessible within class DOMWrapper<NodeType>
        await checkbox.setChecked(true);

        const saveButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Create Role'))[0];
        await saveButton.trigger('click');

        const emitted = wrapper.emitted('save');
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toBe('PUBLISHER');
        expect(emitted![0][1]).toHaveLength(1);
    });
});
