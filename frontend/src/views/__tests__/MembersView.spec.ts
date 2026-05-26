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
