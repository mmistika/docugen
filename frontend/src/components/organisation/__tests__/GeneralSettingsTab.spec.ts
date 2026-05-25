import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import GeneralSettingsTab from '../GeneralSettingsTab.vue';
import { api } from '@/api/client';
import { useOrgStore } from '@/stores/org';

vi.mock('@/api/client', () => ({
    api: {
        organisations: {
            my: vi.fn(),
            rename: vi.fn()
        }
    }
}));

describe('GeneralSettingsTab.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('renders loading initially and fetches org details', async () => {
        vi.mocked(api.organisations.my).mockResolvedValue([
            { id: 4, name: 'Initial Org', memberCount: 1 }
        ]);

        const wrapper = mount(GeneralSettingsTab, {
            props: { orgId: 4 }
        });

        expect(wrapper.text()).toContain('Loading settings…');

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.my).toHaveBeenCalled();
        expect(wrapper.text()).toContain('General Settings');
        const input = wrapper.find('input[placeholder="Organisation name"]');
        expect((input.element as HTMLInputElement).value).toBe('Initial Org');
    });

    it('validates empty organization name', async () => {
        vi.mocked(api.organisations.my).mockResolvedValue([
            { id: 4, name: 'Initial Org', memberCount: 1 }
        ]);

        const wrapper = mount(GeneralSettingsTab, {
            props: { orgId: 4 }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const input = wrapper.find('input[placeholder="Organisation name"]');
        await input.setValue('');

        const saveButton = wrapper.find('button');
        await saveButton.trigger('click');

        expect(wrapper.text()).toContain('Organisation name cannot be empty.');
        expect(api.organisations.rename).not.toHaveBeenCalled();
    });

    it('calls rename API and refreshes org store on save', async () => {
        vi.mocked(api.organisations.my).mockResolvedValue([
            { id: 4, name: 'Initial Org', memberCount: 1 }
        ]);
        vi.mocked(api.organisations.rename).mockResolvedValue();

        const orgStore = useOrgStore();
        const orgStoreFetchSpy = vi
            .spyOn(orgStore, 'fetch')
            .mockResolvedValue();

        const wrapper = mount(GeneralSettingsTab, {
            props: { orgId: 4 }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const input = wrapper.find('input[placeholder="Organisation name"]');
        await input.setValue('Renamed Org');

        const saveButton = wrapper.find('button');
        await saveButton.trigger('click');

        expect(api.organisations.rename).toHaveBeenCalledWith(4, {
            name: 'Renamed Org'
        });
        expect(orgStoreFetchSpy).toHaveBeenCalled();
    });
});
