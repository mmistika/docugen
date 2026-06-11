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
import { createPinia, setActivePinia } from 'pinia';
import { useNotificationStore } from '../notification';

describe('Notification Store', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should initialize with an empty list of toasts', () => {
        const store = useNotificationStore();
        expect(store.toasts).toEqual([]);
    });

    it('should add a toast with default values', () => {
        const store = useNotificationStore();
        store.addToast('Test Message');

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]).toEqual({
            id: expect.any(String),
            text: 'Test Message',
            type: 'info',
            duration: 4000
        });
    });

    it('should auto-remove toast after duration', () => {
        const store = useNotificationStore();
        store.addToast('Will disappear', 'info', 2000);

        expect(store.toasts.length).toBe(1);
        vi.advanceTimersByTime(2000);
        expect(store.toasts.length).toBe(0);
    });

    it('should not auto-remove toast if duration is 0 or negative', () => {
        const store = useNotificationStore();
        store.addToast('Stays forever', 'success', 0);

        expect(store.toasts.length).toBe(1);
        vi.advanceTimersByTime(10000);
        expect(store.toasts.length).toBe(1);
    });

    it('should manually remove a toast by id', () => {
        const store = useNotificationStore();
        store.addToast('Toast 1', 'info', 0);
        store.addToast('Toast 2', 'success', 0);

        expect(store.toasts.length).toBe(2);

        const firstId = store.toasts[0]!.id;
        store.removeToast(firstId);

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]!.text).toBe('Toast 2');
    });

    it('should support success helper method', () => {
        const store = useNotificationStore();
        store.success('Success alert', 1000);

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]).toEqual({
            id: expect.any(String),
            text: 'Success alert',
            type: 'success',
            duration: 1000
        });
    });

    it('should support error helper method', () => {
        const store = useNotificationStore();
        store.error('Error alert');

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]).toEqual({
            id: expect.any(String),
            text: 'Error alert',
            type: 'error',
            duration: 4000
        });
    });

    it('should support warning helper method', () => {
        const store = useNotificationStore();
        store.warning('Warning alert');

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]!.type).toBe('warning');
    });

    it('should support info helper method', () => {
        const store = useNotificationStore();
        store.info('Info alert');

        expect(store.toasts.length).toBe(1);
        expect(store.toasts[0]!.type).toBe('info');
    });
});
