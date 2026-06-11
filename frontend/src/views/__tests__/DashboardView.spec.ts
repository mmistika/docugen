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

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import DashboardView from '../DashboardView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            dashboard: {
                get: vi.fn()
            }
        }
    }
}));

describe('DashboardView.vue', () => {
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
        return mount(DashboardView, {
            global: {
                stubs: {
                    TabHeader: true
                }
            }
        });
    };

    it('renders loading state initially', async () => {
        vi.mocked(api.organisations.dashboard.get).mockReturnValue(
            new Promise(() => {})
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 10;

        const wrapper = createWrapper();

        expect(wrapper.text()).toContain('Loading statistics...');
    });

    it('displays access denied when statistics endpoint returns 403', async () => {
        const err = { response: { status: 403 } };
        vi.mocked(api.organisations.dashboard.get).mockRejectedValue(err);

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 10;

        const wrapper = createWrapper();

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Access Restrained');
        expect(wrapper.text()).toContain(
            'You do not have the permission required'
        );
    });

    it('renders stats and activities when statistics load successfully', async () => {
        const mockDashboardData = {
            totalUsers: 5,
            totalTemplates: 12,
            totalDocuments: 154,
            recentActivity: [
                {
                    id: 1,
                    action: 'document:create',
                    userName: 'John Doe',
                    timestamp: new Date().toISOString(),
                    isToken: false
                }
            ],
            usageTrends: []
        };
        vi.mocked(api.organisations.dashboard.get).mockResolvedValue(
            mockDashboardData as any
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 10;

        const wrapper = createWrapper();

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('5');
        expect(wrapper.text()).toContain('12');
        expect(wrapper.text()).toContain('154');
        expect(wrapper.text()).toContain('Document:Create');
        expect(wrapper.text()).toContain('by John Doe');
    });
});
