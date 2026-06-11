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
import AuditView from '../AuditView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            audit: {
                get: vi.fn()
            }
        }
    }
}));

describe('AuditView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const mockLogsResponse = {
        content: [
            {
                id: 1,
                action: 'document:create',
                userName: 'John Doe',
                userEmail: 'john@example.com',
                timestamp: '2026-06-03T12:00:00Z',
                entityType: 'Document',
                entityId: 101,
                metadata: '{}',
                isToken: false
            }
        ],
        page: {
            totalElements: 1,
            totalPages: 1,
            number: 0,
            size: 10
        }
    };

    it('loads audit logs from API and displays them', async () => {
        vi.mocked(api.organisations.audit.get).mockResolvedValue(
            mockLogsResponse
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = mount(AuditView, {
            global: {
                stubs: {
                    TabHeader: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.audit.get).toHaveBeenCalledWith(
            1,
            expect.any(Object)
        );
        expect(wrapper.text()).toContain('John Doe');
        expect(wrapper.text()).toContain('john@example.com');
        expect(wrapper.text()).toContain('document:create');
    });

    it('applies from and to dates parameters when applying filters', async () => {
        vi.mocked(api.organisations.audit.get).mockResolvedValue(
            mockLogsResponse
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 1;

        const wrapper = mount(AuditView, {
            global: {
                stubs: { TabHeader: true }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const inputs = wrapper.findAll('input[type="datetime-local"]');
        await inputs[0].setValue('2026-06-01T00:00');
        await inputs[1].setValue('2026-06-02T23:59');

        const applyBtn = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Apply'))[0];
        await applyBtn.trigger('click');

        expect(api.organisations.audit.get).toHaveBeenLastCalledWith(
            1,
            expect.objectContaining({
                from: new Date('2026-06-01T00:00').toISOString(),
                to: new Date('2026-06-02T23:59').toISOString()
            })
        );
    });
});
