import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface ToastMessage {
    id: string;
    type: 'success' | 'error' | 'info' | 'warning';
    text: string;
    duration?: number;
}

export const useNotificationStore = defineStore('notification', () => {
    const toasts = ref<ToastMessage[]>([]);

    function addToast(
        text: string,
        type: ToastMessage['type'] = 'info',
        duration = 4000
    ) {
        const id = Math.random().toString(36).substring(2, 9);
        const toast: ToastMessage = { id, type, text, duration };
        toasts.value.push(toast);

        if (duration > 0) {
            setTimeout(() => {
                removeToast(id);
            }, duration);
        }
    }

    function removeToast(id: string) {
        toasts.value = toasts.value.filter((t) => t.id !== id);
    }

    function success(text: string, duration?: number) {
        addToast(text, 'success', duration);
    }

    function error(text: string, duration?: number) {
        addToast(text, 'error', duration);
    }

    function warning(text: string, duration?: number) {
        addToast(text, 'warning', duration);
    }

    function info(text: string, duration?: number) {
        addToast(text, 'info', duration);
    }

    return {
        toasts,
        addToast,
        removeToast,
        success,
        error,
        warning,
        info
    };
});
