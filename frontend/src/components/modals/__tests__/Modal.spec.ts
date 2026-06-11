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
