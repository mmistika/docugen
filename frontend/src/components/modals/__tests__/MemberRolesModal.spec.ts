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
import MemberRolesModal from '../MemberRolesModal.vue';
import type { Member } from '@/types/member';

describe('MemberRolesModal.vue', () => {
    const availableRoles = ['ADMIN', 'MEMBER', 'VIEWER'];
    const member: Member = {
        id: 10,
        email: 'bob@example.com',
        name: 'Bob',
        surname: 'Smith',
        roles: ['MEMBER']
    };

    it('renders and highlights current member roles', async () => {
        const wrapper = mount(MemberRolesModal, {
            props: {
                show: false,
                member,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        await wrapper.setProps({ show: true });
        expect(wrapper.text()).toContain('Manage Roles — Bob Smith');

        const memberLabel = wrapper
            .findAll('label')
            .find((l) => l.text().includes('MEMBER'))!;
        expect(memberLabel.classes()).toContain('border-gray-900');
        expect(memberLabel.classes()).toContain('bg-gray-50');

        const adminLabel = wrapper
            .findAll('label')
            .find((l) => l.text().includes('ADMIN'))!;
        expect(adminLabel.classes()).toContain('border-gray-200');
    });

    it('validates that at least one role must be selected', async () => {
        const wrapper = mount(MemberRolesModal, {
            props: {
                show: false,
                member,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        await wrapper.setProps({ show: true });

        const memberLabel = wrapper
            .findAll('label')
            .find((l) => l.text().includes('MEMBER'))!;
        await memberLabel.find('div.w-4').trigger('click');

        const saveButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Save Roles'))[0];
        await saveButton.trigger('click');

        expect(wrapper.text()).toContain('At least one role must be assigned.');
        expect(wrapper.emitted('save')).toBeFalsy();
    });

    it('emits save event with memberId and updated roles on submit', async () => {
        const wrapper = mount(MemberRolesModal, {
            props: {
                show: false,
                member,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        await wrapper.setProps({ show: true });

        const adminLabel = wrapper
            .findAll('label')
            .find((l) => l.text().includes('ADMIN'))!;
        await adminLabel.find('div.w-4').trigger('click');

        const saveButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Save Roles'))[0];
        await saveButton.trigger('click');

        const emitted = wrapper.emitted('save');
        expect(emitted).toBeTruthy();
        expect(emitted![0][0]).toBe(10);

        const roles = emitted![0][1] as string[];
        expect(roles).toContain('MEMBER');
        expect(roles).toContain('ADMIN');
    });
});
