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
