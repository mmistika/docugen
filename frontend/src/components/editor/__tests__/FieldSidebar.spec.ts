import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FieldSidebar from '../FieldSidebar.vue';
import type { Field } from '@/types/field';

describe('FieldSidebar.vue', () => {
    const globalFields: Field[] = [
        { id: 'g1', name: 'Global1', type: 'text', required: true }
    ];
    const inlineFields: Field[] = [
        { id: 'i1', name: 'Inline1', type: 'number', required: false }
    ];

    it('renders global and inline field sections', () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null,
                fieldsExpanded: true,
                inlineFieldsExpanded: true
            }
        });

        expect(wrapper.text()).toContain('Global Fields');
        expect(wrapper.text()).toContain('Inline Embeds');
        expect(wrapper.text()).toContain('Global1');
        expect(wrapper.text()).toContain('Inline1');
    });

    it('emits add-global when a global field button is clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields: [],
                inlineFields: [],
                selectedFieldId: null,
                fieldsExpanded: false,
                inlineFieldsExpanded: false
            }
        });

        const globalBtn = wrapper.findAll('.flex-col').at(0)?.find('button');
        await globalBtn?.trigger('click');

        expect(wrapper.emitted('add-global')).toBeTruthy();
    });

    it('emits select-field when field row is clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null,
                fieldsExpanded: true,
                inlineFieldsExpanded: true
            }
        });

        const fieldBtn = wrapper
            .findAll('button')
            .find((b) => b.text().includes('Global1'))!;
        await fieldBtn.trigger('click');

        expect(wrapper.emitted('select-field')?.[0]).toEqual(['g1']);
    });

    it('emits update:fieldsExpanded and update:inlineFieldsExpanded when toggles clicked', async () => {
        const wrapper = mount(FieldSidebar, {
            props: {
                globalFields,
                inlineFields,
                selectedFieldId: null,
                fieldsExpanded: true,
                inlineFieldsExpanded: true
            }
        });

        const toggleButtons = wrapper
            .findAll('button')
            .filter(
                (b) =>
                    b.text().includes('Fields') ||
                    b.text().includes('Inline Fields')
            );

        await toggleButtons[0].trigger('click');
        expect(wrapper.emitted('update:fieldsExpanded')?.[0]).toEqual([false]);

        await toggleButtons[1].trigger('click');
        expect(wrapper.emitted('update:inlineFieldsExpanded')?.[0]).toEqual([
            false
        ]);
    });
});
