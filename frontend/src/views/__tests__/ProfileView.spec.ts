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
import ProfileView from '../ProfileView.vue';
import { useAuthStore } from '@/stores/auth';

describe('ProfileView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);

        const authStore = useAuthStore();
        authStore.user = {
            userId: 1,
            email: 'john.doe@example.com',
            name: 'John',
            surname: 'Doe',
            image: 'data:image/png;base64,mocked-base64',
            registered: true
        };
    });

    const createWrapper = () => {
        return mount(ProfileView, {
            global: {
                stubs: {
                    RouterLink: true
                }
            }
        });
    };

    it('should pre-populate name and surname from store', () => {
        const wrapper = createWrapper();
        const nameInput = wrapper.find('input[placeholder="Name"]');
        const surnameInput = wrapper.find('input[placeholder="Surname"]');

        expect((nameInput.element as HTMLInputElement).value).toBe('John');
        expect((surnameInput.element as HTMLInputElement).value).toBe('Doe');
    });

    it('should disable save button initially when no changes are present', () => {
        const wrapper = createWrapper();
        const saveButton = wrapper.find('button[class*="bg-gray-900"]');

        expect(saveButton.attributes()).toHaveProperty('disabled');
    });

    it('should enable save button when name is changed', async () => {
        const wrapper = createWrapper();
        const nameInput = wrapper.find('input[placeholder="Name"]');
        await nameInput.setValue('Johnny');

        const saveButton = wrapper.find('button[class*="bg-gray-900"]');
        expect(saveButton.attributes()).not.toHaveProperty('disabled');
    });

    it('should disable save button if name is cleared', async () => {
        const wrapper = createWrapper();
        const nameInput = wrapper.find('input[placeholder="Name"]');
        await nameInput.setValue('');

        const saveButton = wrapper.find('button[class*="bg-gray-900"]');
        expect(saveButton.attributes()).toHaveProperty('disabled');
    });

    it('should invoke authStore.updateProfile when save button is clicked', async () => {
        const wrapper = createWrapper();
        const authStore = useAuthStore();

        const updateProfileSpy = vi
            .spyOn(authStore, 'updateProfile')
            .mockResolvedValue(undefined as any);

        const nameInput = wrapper.find('input[placeholder="Name"]');
        await nameInput.setValue('Johnny');

        const saveButton = wrapper.find('button[class*="bg-gray-900"]');
        await saveButton.trigger('click');

        expect(updateProfileSpy).toHaveBeenCalledWith(
            'Johnny',
            'Doe',
            'data:image/png;base64,mocked-base64'
        );
    });
});
