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
import type { Organisation } from '@/types/organisation.ts';

interface OrgState {
    organisations: Organisation[];
    currentOrgId: number | null;
}

export const useOrgStore = defineStore('org', {
    state: (): OrgState => ({
        organisations: [],
        currentOrgId: null
    }),

    getters: {
        currentOrg(state): Organisation | undefined {
            return state.organisations.find((o) => o.id === state.currentOrgId);
        }
    },

    actions: {
        async fetch(): Promise<void> {
            const data = await api.organisations.my();
            this.organisations = data;

            if (!this.currentOrgId && data.length > 0) {
                this.currentOrgId = data[0]?.id || null;
            }
        },

        setCurrentOrg(id: number) {
            this.currentOrgId = id;
        }
    }
});
