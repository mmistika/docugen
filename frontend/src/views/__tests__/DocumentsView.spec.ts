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
import DocumentsView from '../DocumentsView.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            documents: {
                all: vi.fn(),
                view: vi.fn(),
                finalise: vi.fn(),
                revertToDraft: vi.fn()
            }
        }
    }
}));

describe('DocumentsView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();

        vi.stubGlobal('URL', {
            createObjectURL: vi.fn(() => 'blob:http://localhost/mock-blob'),
            revokeObjectURL: vi.fn()
        });
        vi.stubGlobal('open', vi.fn());
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    const mockDocs = [
        {
            id: 1,
            name: 'Doc A',
            templateName: 'Template 1',
            createdAt: '2026-06-01T12:00:00Z',
            status: 'DRAFT',
            templateId: 101
        },
        {
            id: 2,
            name: 'Doc B',
            templateName: 'Template 2',
            createdAt: '2026-06-02T12:00:00Z',
            status: 'FINAL',
            templateId: 102
        }
    ];

    const mockPagedResponse = {
        content: mockDocs,
        page: {
            totalElements: mockDocs.length,
            totalPages: 1,
            number: 0,
            size: 10
        }
    };

    it('loads documents and lists them in table', async () => {
        vi.mocked(api.organisations.documents.all).mockResolvedValue(
            mockPagedResponse as any
        );

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 5;

        const wrapper = mount(DocumentsView, {
            global: {
                stubs: {
                    RouterLink: true,
                    TabHeader: true,
                    SearchFilterBar: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.documents.all).toHaveBeenCalledWith(5, {
            page: 0,
            size: 10
        });
        expect(wrapper.text()).toContain('Doc A');
        expect(wrapper.text()).toContain('Doc B');
    });

    it('finalises draft document when click finalise button', async () => {
        vi.mocked(api.organisations.documents.all).mockResolvedValue(
            mockPagedResponse as any
        );
        vi.mocked(api.organisations.documents.finalise).mockResolvedValue();

        const orgStore = useOrgStore();
        orgStore.currentOrgId = 5;

        const wrapper = mount(DocumentsView, {
            global: {
                stubs: {
                    RouterLink: true,
                    TabHeader: true,
                    SearchFilterBar: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const finaliseBtn = wrapper.find('button[title="Finalise document"]');
        await finaliseBtn.trigger('click');

        expect(api.organisations.documents.finalise).toHaveBeenCalledWith(5, 1);
    });
});
