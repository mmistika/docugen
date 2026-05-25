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
