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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import MembersView from '../MembersView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        users: {
            invite: vi.fn()
        },
        organisations: {
            members: {
                all: vi.fn(),
                updateRoles: vi.fn()
            },
            rbac: {
                roles: vi.fn()
            }
        }
    }
}));

describe('MembersView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const mockMembers = [
        {
            id: 1,
            email: 'john@example.com',
            name: 'John',
            surname: 'Doe',
            roles: ['ADMIN'],
            orgId: 1
        }
    ];

    const mockRoles = [
        { name: 'ADMIN', permissions: [] },
        { name: 'MEMBER', permissions: [] }
    ];

    it('loads and renders members list', async () => {
        vi.mocked(api.organisations.members.all).mockResolvedValue(mockMembers);
        vi.mocked(api.organisations.rbac.roles).mockResolvedValue(mockRoles);

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = mount(MembersView, {
            global: {
                stubs: {
                    TabHeader: true,
                    SearchFilterBar: true,
                    InviteMemberModal: true,
                    ManageMemberRolesModal: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.members.all).toHaveBeenCalledWith(1);
        expect(api.organisations.rbac.roles).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain('John Doe');
        expect(wrapper.text()).toContain('john@example.com');
    });

    it('invokes invite API on handleInvite', async () => {
        vi.mocked(api.organisations.members.all).mockResolvedValue(mockMembers);
        vi.mocked(api.organisations.rbac.roles).mockResolvedValue([]);
        vi.mocked(api.users.invite).mockResolvedValue();

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = mount(MembersView, {
            global: {
                stubs: {
                    TabHeader: true,
                    SearchFilterBar: true,
                    InviteMemberModal: true,
                    ManageMemberRolesModal: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        await (wrapper.vm as any).handleInvite('test@example.com', 'MEMBER');

        expect(api.users.invite).toHaveBeenCalledWith({
            orgId: 1,
            email: 'test@example.com',
            role: 'MEMBER'
        });
    });
});
