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
