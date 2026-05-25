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

    it('loads documents and lists them in table', async () => {
        vi.mocked(api.organisations.documents.all).mockResolvedValue(mockDocs);

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

        expect(api.organisations.documents.all).toHaveBeenCalledWith(5);
        expect(wrapper.text()).toContain('Doc A');
        expect(wrapper.text()).toContain('Doc B');
    });

    it('finalises draft document when click finalise button', async () => {
        vi.mocked(api.organisations.documents.all).mockResolvedValue(mockDocs);
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
