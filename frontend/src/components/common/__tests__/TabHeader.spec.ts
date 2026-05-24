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
