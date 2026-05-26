import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import OrganisationsView from '../OrganisationsView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            create: vi.fn()
        }
    }
}));

describe('OrganisationsView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('renders organisations list from store and handles filters', async () => {
        const orgStore = useOrgStore();
        orgStore.organisations = [
            { id: 1, name: 'Acme Corp', memberCount: 1 },
            { id: 2, name: 'Cyber Systems', memberCount: 1 }
        ];

        const wrapper = mount(OrganisationsView, {
            global: {
                stubs: {
                    RouterLink: { template: '<a><slot /></a>' },
                    TabHeader: true,
                    SearchFilterBar: {
                        props: ['modelValue'],
                        template:
                            '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
                    },
                    CreateOrganisationModal: true
                }
            }
        });

        expect(wrapper.text()).toContain('Acme Corp');
        expect(wrapper.text()).toContain('Cyber Systems');

        const searchInput = wrapper.find('input');
        await searchInput.setValue('Acme');

        expect(wrapper.text()).toContain('Acme Corp');
        expect(wrapper.text()).not.toContain('Cyber Systems');
    });

    it('submits organization creation request and updates list', async () => {
        const orgStore = useOrgStore();
        orgStore.organisations = [];
        const orgStoreFetchSpy = vi
            .spyOn(orgStore, 'fetch')
            .mockResolvedValue();
        vi.mocked(api.organisations.create).mockResolvedValue();

        const wrapper = mount(OrganisationsView, {
            global: {
                stubs: {
                    RouterLink: { template: '<a><slot /></a>' },
                    TabHeader: true,
                    SearchFilterBar: true,
                    CreateOrganisationModal: true
                }
            }
        });

        await (wrapper.vm as any).createOrganisation('New Corp');

        expect(api.organisations.create).toHaveBeenCalledWith({
            name: 'New Corp'
        });
        expect(orgStoreFetchSpy).toHaveBeenCalled();
    });
});
