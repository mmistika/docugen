import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ToastContainer from '../ToastContainer.vue';
import { useNotificationStore } from '@/stores/notification';

describe('ToastContainer.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
    });

    it('renders list of toasts from the store', () => {
        const notificationStore = useNotificationStore();
        notificationStore.toasts = [
            { id: '1', text: 'Success notification', type: 'success' },
            { id: '2', text: 'Error notification', type: 'error' }
        ];

        const wrapper = mount(ToastContainer, {
            global: {
                stubs: {
                    TransitionGroup: false
                }
            }
        });

        expect(wrapper.text()).toContain('Success notification');
        expect(wrapper.text()).toContain('Error notification');
    });

    it('calls removeToast when close button is clicked', async () => {
        const notificationStore = useNotificationStore();
        notificationStore.toasts = [
            { id: '42', text: 'Click me to close', type: 'info' }
        ];

        const removeSpy = vi.spyOn(notificationStore, 'removeToast');

        const wrapper = mount(ToastContainer);
        const closeBtn = wrapper.find('button');
        await closeBtn.trigger('click');

        expect(removeSpy).toHaveBeenCalledWith('42');
    });
});
