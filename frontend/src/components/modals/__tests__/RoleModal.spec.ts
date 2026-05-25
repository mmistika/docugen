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
