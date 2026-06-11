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
import InviteMemberModal from '../InviteMemberModal.vue';

describe('InviteMemberModal.vue', () => {
    const availableRoles = ['ADMIN', 'MEMBER', 'VIEWER'];

    it('renders input, select, and options correctly when show is true', () => {
        const wrapper = mount(InviteMemberModal, {
            props: {
                show: true,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        expect(wrapper.text()).toContain('Invite Member');
        const input = wrapper.find('input[type="email"]');
        expect(input.exists()).toBe(true);

        const select = wrapper.find('select');
        expect(select.exists()).toBe(true);
        const options = select.findAll('option');
        expect(options).toHaveLength(4);
    });

    it('validates email format and sets error message', async () => {
        const wrapper = mount(InviteMemberModal, {
            props: {
                show: true,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const input = wrapper.find('input[type="email"]');
        await input.setValue('invalid-email');

        const inviteButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Invite'))[0];
        await inviteButton.trigger('click');

        expect(wrapper.text()).toContain('Enter a valid email address.');
        expect(wrapper.emitted('invite')).toBeFalsy();
    });

    it('emits invite event when email and role are valid', async () => {
        const wrapper = mount(InviteMemberModal, {
            props: {
                show: true,
                availableRoles,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const input = wrapper.find('input[type="email"]');
        await input.setValue('john.doe@example.com');

        const select = wrapper.find('select');
        await select.setValue('MEMBER');

        const inviteButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Invite'))[0];
        await inviteButton.trigger('click');

        expect(wrapper.emitted('invite')?.[0]).toEqual([
            'john.doe@example.com',
            'MEMBER'
        ]);
    });
});
