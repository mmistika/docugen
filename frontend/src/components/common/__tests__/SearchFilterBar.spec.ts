import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import SearchFilterBar from '../SearchFilterBar.vue';

describe('SearchFilterBar.vue', () => {
    it('renders search input with custom placeholder', () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: '',
                placeholder: 'Find files...'
            }
        });
        const input = wrapper.find('input');
        expect(input.attributes('placeholder')).toBe('Find files...');
    });

    it('emits update:modelValue when text is typed', async () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: ''
            }
        });
        const input = wrapper.find('input');
        await input.setValue('document');
        expect(wrapper.emitted('update:modelValue')?.[0][0]).toBe('document');
    });

    it('shows clear button and clears search when clicked', async () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: 'hello'
            }
        });

        const clearBtn = wrapper.find('button[title="Clear search"]');
        expect(clearBtn.exists()).toBe(true);

        await clearBtn.trigger('click');
        expect(wrapper.emitted('update:modelValue')?.[0][0]).toBe('');
    });

    it('renders custom filters slot', () => {
        const wrapper = mount(SearchFilterBar, {
            props: {
                modelValue: ''
            },
            slots: {
                filters: '<button id="filter-btn">Filter</button>'
            }
        });
        expect(wrapper.find('#filter-btn').exists()).toBe(true);
    });
});
