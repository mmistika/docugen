import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FieldTypeButton from '../FieldTypeButton.vue';
import type { FieldTypeDef } from '@/types/field';

describe('FieldTypeButton.vue', () => {
    const def: FieldTypeDef = {
        type: 'text',
        label: 'Text Box',
        icon: 'Type',
        defaults: {},
        properties: []
    };

    it('renders the label correctly', () => {
        const wrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });
        expect(wrapper.text()).toContain('Text Box');
    });

    it('applies correct CSS classes based on the variant prop', () => {
        const globalWrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });
        expect(globalWrapper.classes()).toContain('bg-gray-50');

        const inlineWrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'inline'
            }
        });
        expect(inlineWrapper.classes()).toContain('bg-blue-50');
    });

    it('emits click event with correct field type', async () => {
        const wrapper = mount(FieldTypeButton, {
            props: {
                def,
                variant: 'global'
            }
        });

        await wrapper.trigger('click');
        expect(wrapper.emitted('click')?.[0]).toEqual(['text']);
    });
});
