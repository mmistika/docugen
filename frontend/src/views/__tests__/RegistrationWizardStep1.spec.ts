import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import RegistrationWizardStep1 from '../RegistrationWizardStep1.vue';
import { useAuthStore } from '@/stores/auth';

const routerPushSpy = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: routerPushSpy
    })
}));

describe('RegistrationWizardStep1.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('renders step 1 inputs and disables finish button initially', () => {
        const wrapper = mount(RegistrationWizardStep1);

        expect(wrapper.text()).toContain('Complete your profile');

        const finishBtn = wrapper.find('button[type="submit"]');
        expect(finishBtn.attributes()).toHaveProperty('disabled');
    });

    it('enables finish button when inputs are populated validly', async () => {
        const wrapper = mount(RegistrationWizardStep1);

        const nameInput = wrapper.find('#name');
        const surnameInput = wrapper.find('#surname');

        await nameInput.setValue('Alice');
        await surnameInput.setValue('Smith');

        const finishBtn = wrapper.find('button[type="submit"]');
        expect(finishBtn.attributes()).not.toHaveProperty('disabled');
    });

    it('completes profile and routes to home on submit', async () => {
        const wrapper = mount(RegistrationWizardStep1);
        const authStore = useAuthStore();
        const completeSpy = vi
            .spyOn(authStore, 'completeProfile')
            .mockResolvedValue(undefined as any);

        const nameInput = wrapper.find('#name');
        const surnameInput = wrapper.find('#surname');

        await nameInput.setValue('Alice');
        await surnameInput.setValue('Smith');

        const form = wrapper.find('form');
        await form.trigger('submit.prevent');

        expect(completeSpy).toHaveBeenCalledWith('Alice', 'Smith', null);
        expect(routerPushSpy).toHaveBeenCalledWith('/');
    });
});
