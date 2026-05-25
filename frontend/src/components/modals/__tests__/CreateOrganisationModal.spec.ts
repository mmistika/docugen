import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import CreateOrganisationModal from '../CreateOrganisationModal.vue';

describe('CreateOrganisationModal.vue', () => {
    it('renders input and buttons correctly when show is true', () => {
        const wrapper = mount(CreateOrganisationModal, {
            props: {
                show: true,
                isSubmitting: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        expect(wrapper.text()).toContain('Create New Organization');
        const input = wrapper.find('input');
        expect(input.exists()).toBe(true);
    });

    it('emits create event on form submission with trimmed text', async () => {
        const wrapper = mount(CreateOrganisationModal, {
            props: {
                show: true,
                isSubmitting: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const input = wrapper.find('input');
        await input.setValue('  My Cool Org  ');

        const form = wrapper.find('form');
        await form.trigger('submit.prevent');

        expect(wrapper.emitted('create')?.[0]).toEqual(['My Cool Org']);
    });

    it('emits close event when cancel button is clicked', async () => {
        const wrapper = mount(CreateOrganisationModal, {
            props: {
                show: true,
                isSubmitting: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const cancelButton = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Cancel'))[0];
        await cancelButton.trigger('click');

        expect(wrapper.emitted('close')).toBeTruthy();
    });
});
