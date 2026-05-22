import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import DataTable from '../DataTable.vue';

describe('DataTable.vue', () => {
    const headers = [
        { key: 'name', label: 'Name' },
        { key: 'role', label: 'Role' }
    ];
    const items = [
        { id: 1, name: 'Alice', role: 'Admin' },
        { id: 2, name: 'Bob', role: 'User' }
    ];

    it('renders a loading message when isLoading is true', () => {
        const wrapper = mount(DataTable, {
            props: {
                headers,
                items: [],
                isLoading: true
            }
        });
        expect(wrapper.text()).toContain('Loading records…');
    });

    it('renders default empty text when items are empty', () => {
        const wrapper = mount(DataTable, {
            props: {
                headers,
                items: [],
                isLoading: false
            }
        });
        expect(wrapper.text()).toContain('No records found.');
    });

    it('renders custom empty text when emptyText prop is provided', () => {
        const wrapper = mount(DataTable, {
            props: {
                headers,
                items: [],
                isLoading: false,
                emptyText: 'Custom empty message'
            }
        });
        expect(wrapper.text()).toContain('Custom empty message');
    });

    it('renders headers and items in the table', () => {
        const wrapper = mount(DataTable, {
            props: {
                headers,
                items,
                isLoading: false
            }
        });

        const ths = wrapper.findAll('thead th');
        expect(ths).toHaveLength(2);
        expect(ths[0].text()).toBe('Name');
        expect(ths[1].text()).toBe('Role');

        const rows = wrapper.findAll('tbody tr');
        expect(rows).toHaveLength(2);
        expect(rows[0].text()).toContain('Alice');
        expect(rows[0].text()).toContain('Admin');
        expect(rows[1].text()).toContain('Bob');
        expect(rows[1].text()).toContain('User');
    });

    it('supports slot overrides for cells', () => {
        const wrapper = mount(DataTable, {
            props: {
                headers,
                items,
                isLoading: false
            },
            slots: {
                'cell:role': `<template #default="props">Custom role: {{ props.item.role }}</template>`
            }
        });
        expect(wrapper.text()).toContain('Custom role: Admin');
        expect(wrapper.text()).toContain('Custom role: User');
    });
});
