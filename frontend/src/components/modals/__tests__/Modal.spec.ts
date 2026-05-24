import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import Modal from '../Modal.vue';

describe('Modal.vue', () => {
    it('does not render when show is false', () => {
        const wrapper = mount(Modal, {
            props: {
                show: false,
                title: 'Test Modal'
            },
            global: {
                stubs: {
                    Teleport: {
                        template: '<div><slot /></div>'
                    }
                }
            }
        });
        expect(wrapper.text()).toBe('');
    });

    it('renders title and slot content when show is true', () => {
        const wrapper = mount(Modal, {
            props: {
                show: true,
                title: 'Add User'
            },
            slots: {
                body: '<div id="modal-body">Form content</div>',
                footer: '<button id="save-btn">Save</button>'
            },
            global: {
                stubs: {
                    Teleport: {
                        template: '<div><slot /></div>'
                    }
                }
            }
        });

        expect(wrapper.text()).toContain('Add User');
        expect(wrapper.find('#modal-body').exists()).toBe(true);
        expect(wrapper.find('#save-btn').text()).toBe('Save');
    });

    it('emits close event when close button or overlay is clicked', async () => {
        const wrapper = mount(Modal, {
            props: {
                show: true,
                title: 'Test'
            },
            global: {
                stubs: {
                    Teleport: {
                        template: '<div><slot /></div>'
                    }
                }
            }
        });

        const closeButton = wrapper.find('button');
        await closeButton.trigger('click');
        expect(wrapper.emitted('close')).toBeTruthy();

        const overlay = wrapper.find('.fixed.inset-0');
        await overlay.trigger('click');
        expect(wrapper.emitted('close')).toHaveLength(2);
    });
});
