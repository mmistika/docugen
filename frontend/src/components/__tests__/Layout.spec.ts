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
import Layout from '../Layout.vue';
import { useAuthStore } from '@/stores/auth';
import { useOrgStore } from '@/stores/org';

// Mock localStorage
const storage: Record<string, string> = {};
const localStorageMock = {
    getItem: vi.fn((key: string) => storage[key] || null),
    setItem: vi.fn((key: string, value: string) => {
        storage[key] = value;
    }),
    clear: vi.fn(() => {
        for (const key in storage) {
            delete storage[key];
        }
    }),
    removeItem: vi.fn((key: string) => {
        delete storage[key];
    }),
    key: vi.fn(() => null),
    length: 0
};
vi.stubGlobal('localStorage', localStorageMock);

// Mock vue-router
const mockRoute = {
    path: '/dashboard'
};
vi.mock('vue-router', () => ({
    useRoute: () => mockRoute,
    useRouter: () => ({
        push: vi.fn()
    }),
    RouterLink: {
        template: '<a><slot /></a>'
    },
    RouterView: {
        template: '<div><slot /></div>'
    }
}));

describe('Layout.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);

        localStorage.clear();

        // Setup store data
        const authStore = useAuthStore();
        authStore.user = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: '',
            registered: true
        };

        const orgStore = useOrgStore();
        orgStore.organisations = [
            { id: 1, name: 'Org 1', memberCount: 1 },
            { id: 2, name: 'Org 2', memberCount: 1 }
        ];
        orgStore.currentOrgId = 1;
    });

    const createWrapper = () => {
        return mount(Layout, {
            global: {
                stubs: {
                    RouterLink: true,
                    RouterView: true
                }
            }
        });
    };

    it('should render user initials if no image is present', () => {
        const wrapper = createWrapper();
        expect(wrapper.text()).toContain('J');
    });

    it('should display the correct list of organisations in the dropdown', () => {
        const wrapper = createWrapper();
        const options = wrapper.findAll('select option');
        expect(options).toHaveLength(2);
        expect(options[0].text()).toBe('Org 1');
        expect(options[1].text()).toBe('Org 2');
    });

    it('should call orgStore.fetch on mount if organisations are empty', async () => {
        const orgStore = useOrgStore();
        orgStore.organisations = [];
        const fetchSpy = vi.spyOn(orgStore, 'fetch').mockResolvedValue();

        createWrapper();

        expect(fetchSpy).toHaveBeenCalled();
    });

    it('should toggle sidebar and persist state in localStorage', async () => {
        const wrapper = createWrapper();
        const toggleButton = wrapper.find('button[title="Collapse Sidebar"]');

        expect(wrapper.find('aside').classes()).toContain('w-64');

        await toggleButton.trigger('click');

        expect(wrapper.find('aside').classes()).toContain('w-16');
        expect(localStorage.setItem).toHaveBeenCalledWith(
            'sidebar_collapsed',
            'true'
        );
    });
});
