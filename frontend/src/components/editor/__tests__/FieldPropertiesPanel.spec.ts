import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FieldPropertiesPanel from '../FieldPropertiesPanel.vue';
import type { Field } from '@/types/field';

describe('FieldPropertiesPanel.vue', () => {
    it('renders placeholder when no field is selected', () => {
        const wrapper = mount(FieldPropertiesPanel, {
            props: {
                activeField: null,
                isInline: false
            }
        });
        expect(wrapper.text()).toContain('No field selected');
    });

    it('renders basic properties of the selected field', () => {
        const activeField: Field = {
            id: 'f1',
            name: 'ClientName',
            type: 'text',
            required: true
        };
        const wrapper = mount(FieldPropertiesPanel, {
            props: {
                activeField,
                isInline: true
            }
        });

        expect(wrapper.text()).toContain('Field Properties');
        expect(wrapper.text()).toContain('Inline');

        const nameInput = wrapper.find('input[type="text"]');
        expect((nameInput.element as HTMLInputElement).value).toBe(
            'ClientName'
        );

        const requiredCheckbox = wrapper.find('input[type="checkbox"]');
        expect((requiredCheckbox.element as HTMLInputElement).checked).toBe(
            true
        );
    });

    it('emits update event when property changes', async () => {
        const activeField: Field = {
            id: 'f1',
            name: 'ClientName',
            type: 'text',
            required: true
        };
        const wrapper = mount(FieldPropertiesPanel, {
            props: {
                activeField,
                isInline: false
            }
        });

        const nameInput = wrapper.find('input[type="text"]');
        await nameInput.setValue('NewClientName');

        expect(wrapper.emitted('update')).toBeTruthy();
        expect(wrapper.emitted('update')?.[0]).toEqual([
            'name',
            'NewClientName'
        ]);
    });
});
