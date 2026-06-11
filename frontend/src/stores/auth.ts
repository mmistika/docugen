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

import { defineStore } from 'pinia';
import { api } from '@/api/client';
import { auth0 } from '@/auth';
import type { MeResponse } from '@/types/user';

interface AuthState {
    user: MeResponse | null;
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        user: null
    }),
    actions: {
        async fetchMe() {
            this.user = await api.users.me();
        },
        async completeProfile(
            name: string,
            surname: string,
            image: string | null = null
        ) {
            this.user = await api.users.completeRegistration({ name, surname, image });
        },
        async updateProfile(
            name: string,
            surname: string,
            image: string | null
        ) {
            this.user = await api.users.updateProfile({
                name,
                surname,
                image
            });
        },
        async completeOrg(orgName: string) {
            await api.organisations.create({ name: orgName });
            if (this.user) {
                this.user.registered = true;
            }
        },
        async logout() {
            await auth0.logout({
                logoutParams: { returnTo: window.location.origin }
            });
        }
    }
});
