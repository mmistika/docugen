import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import TemplatesView from '../TemplatesView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            templates: {
                all: vi.fn()
            }
        }
    }
}));

describe('TemplatesView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const createWrapper = () => {
        return mount(TemplatesView, {
            global: {
                stubs: {
                    RouterLink: true,
                    TabHeader: {
                        template: '<div><slot name="actions"/></div>'
                    },
                    SearchFilterBar: {
                        props: ['modelValue'],
                        template:
                            '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
                    }
                }
            }
        });
    };

    it('displays loading initially and then renders list of templates', async () => {
        const mockTemplates = [
            { id: 1, name: 'Invoice Template', content: '', manifest: '' },
            { id: 2, name: 'Contract Template', content: '', manifest: '' }
        ];
        vi.mocked(api.organisations.templates.all).mockResolvedValue(
            mockTemplates
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = createWrapper();
        expect(wrapper.text()).toContain('Loading templates...');

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.templates.all).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain('Invoice Template');
        expect(wrapper.text()).toContain('Contract Template');
    });

    it('filters templates list based on search input', async () => {
        const mockTemplates = [
            { id: 1, name: 'Invoice', content: '', manifest: '' },
            { id: 2, name: 'Contract', content: '', manifest: '' }
        ];
        vi.mocked(api.organisations.templates.all).mockResolvedValue(
            mockTemplates
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = createWrapper();
        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const searchInput = wrapper.find('input');
        await searchInput.setValue('Invoice');

        expect(wrapper.text()).toContain('Invoice');
        expect(wrapper.text()).not.toContain('Contract');
    });
});
