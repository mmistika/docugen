import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RegistrationWizardStep2 from '../RegistrationWizardStep2.vue';
import { useAuthStore } from '@/stores/auth';

const routerPushSpy = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: routerPushSpy
    })
}));

describe('RegistrationWizardStep2.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('renders step 2 inputs and disables create button initially', () => {
        const wrapper = mount(RegistrationWizardStep2);

        expect(wrapper.text()).toContain('Create your organisation');

        const createBtn = wrapper.find('button[type="submit"]');
        expect(createBtn.attributes()).toHaveProperty('disabled');
    });

    it('enables create button when input is populated', async () => {
        const wrapper = mount(RegistrationWizardStep2);

        const orgInput = wrapper.find('#orgName');
        await orgInput.setValue('Wayne Enterprises');

        const createBtn = wrapper.find('button[type="submit"]');
        expect(createBtn.attributes()).not.toHaveProperty('disabled');
    });

    it('completes organisation setup and routes to home on submit', async () => {
        const wrapper = mount(RegistrationWizardStep2);
        const authStore = useAuthStore();
        const completeSpy = vi
            .spyOn(authStore, 'completeOrg')
            .mockResolvedValue(undefined as any);

        const orgInput = wrapper.find('#orgName');
        await orgInput.setValue('Wayne Enterprises');

        const form = wrapper.find('form');
        await form.trigger('submit.prevent');

        expect(completeSpy).toHaveBeenCalledWith('Wayne Enterprises');
        expect(routerPushSpy).toHaveBeenCalledWith('/');
    });
});
