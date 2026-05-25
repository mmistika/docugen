import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RbacSettingsTab from '../RbacSettingsTab.vue';
import { api } from '@/api/client';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            rbac: {
                roles: vi.fn(),
                permissions: vi.fn(),
                createRole: vi.fn(),
                updateRole: vi.fn(),
                deleteRole: vi.fn()
            }
        }
    }
}));

describe('RbacSettingsTab.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('renders loading state and then lists roles and their permissions', async () => {
        const mockRoles = [
            { name: 'ADMIN', permissions: ['doc:create', 'doc:delete'] },
            { name: 'VIEWER', permissions: ['doc:view'] }
        ];
        const mockPerms = [
            { id: 1, name: 'doc:create' },
            { id: 2, name: 'doc:delete' },
            { id: 3, name: 'doc:view' }
        ];

        vi.mocked(api.organisations.rbac.roles).mockResolvedValue(mockRoles);
        vi.mocked(api.organisations.rbac.permissions).mockResolvedValue(
            mockPerms
        );

        const wrapper = mount(RbacSettingsTab, {
            props: { orgId: 1 },
            global: {
                stubs: { RoleModal: true }
            }
        });

        expect(wrapper.text()).toContain('Loading roles…');

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('ADMIN');
        expect(wrapper.text()).toContain('VIEWER');
        expect(wrapper.text()).toContain('Create');
        expect(wrapper.text()).toContain('Delete');
    });

    it('prompts confirmation and deletes custom role on confirmation', async () => {
        const mockRoles = [{ name: 'WRITER', permissions: ['doc:create'] }];
        vi.mocked(api.organisations.rbac.roles).mockResolvedValue(mockRoles);
        vi.mocked(api.organisations.rbac.permissions).mockResolvedValue([]);
        vi.mocked(api.organisations.rbac.deleteRole).mockResolvedValue();

        window.confirm = vi.fn().mockReturnValue(true);
        const confirmSpy = vi.spyOn(window, 'confirm');

        const wrapper = mount(RbacSettingsTab, {
            props: { orgId: 1 },
            global: {
                stubs: { RoleModal: true }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const deleteBtn = wrapper.find('button[title="Delete role"]');
        await deleteBtn.trigger('click');

        expect(confirmSpy).toHaveBeenCalled();
        expect(api.organisations.rbac.deleteRole).toHaveBeenCalledWith(
            1,
            'WRITER'
        );
    });
});
