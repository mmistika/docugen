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
import OrganisationSettingsView from '../OrganisationSettingsView.vue';

const routeParams = { id: '4' };
const routerPushSpy = vi.fn();
vi.mock('vue-router', () => ({
    useRoute: () => ({
        params: routeParams,
        name: 'org-settings'
    }),
    useRouter: () => ({
        push: routerPushSpy
    }),
    RouterLink: {
        template: '<a><slot /></a>'
    }
}));

describe('OrganisationSettingsView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('renders the tabs list and displays general settings by default', () => {
        const wrapper = mount(OrganisationSettingsView, {
            global: {
                stubs: {
                    TabHeader: true,
                    GeneralSettingsTab: {
                        template: '<div class="general-mock">General</div>'
                    },
                    RbacSettingsTab: true,
                    ApiTokensSettingsTab: true
                }
            }
        });

        expect(wrapper.text()).toContain('General');
        expect(wrapper.text()).toContain('Roles & Permissions');
        expect(wrapper.text()).toContain('API Tokens');
        expect(wrapper.find('.general-mock').exists()).toBe(true);
    });

    it('changes tabs and displays correct subcomponents on tab click', async () => {
        const wrapper = mount(OrganisationSettingsView, {
            global: {
                stubs: {
                    TabHeader: true,
                    GeneralSettingsTab: true,
                    RbacSettingsTab: {
                        template: '<div class="rbac-mock">Rbac</div>'
                    },
                    ApiTokensSettingsTab: true
                }
            }
        });

        const rbacTabButton = wrapper
            .findAll('nav button')
            .find((b) => b.text().includes('Roles & Permissions'))!;
        await rbacTabButton.trigger('click');

        expect(wrapper.find('.rbac-mock').exists()).toBe(true);
    });
});
