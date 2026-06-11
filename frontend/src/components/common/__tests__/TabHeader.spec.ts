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

import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import TabHeader from '../TabHeader.vue';

vi.mock('vue-router', () => ({
    RouterLink: {
        name: 'RouterLink',
        template: '<a><slot /></a>'
    }
}));

describe('TabHeader.vue', () => {
    it('renders title and description correctly', () => {
        const wrapper = mount(TabHeader, {
            props: {
                title: 'Overview settings',
                description: 'Manage settings here'
            }
        });
        expect(wrapper.find('h1').text()).toBe('Overview settings');
        expect(wrapper.find('p').text()).toBe('Manage settings here');
    });

    it('renders back link when backTo is provided', () => {
        const wrapper = mount(TabHeader, {
            props: {
                title: 'Settings',
                backTo: '/dashboard',
                backLabel: 'Go Home'
            }
        });
        const link = wrapper.findComponent({ name: 'RouterLink' });
        expect(link.exists()).toBe(true);
        expect(link.text()).toBe('Go Home');
    });

    it('renders actions slot content', () => {
        const wrapper = mount(TabHeader, {
            props: {
                title: 'Title'
            },
            slots: {
                actions: '<button id="action-btn">Save</button>'
            }
        });
        expect(wrapper.find('#action-btn').exists()).toBe(true);
    });
});
