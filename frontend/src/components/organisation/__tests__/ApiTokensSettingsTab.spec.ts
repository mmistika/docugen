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
import ApiTokensSettingsTab from '../ApiTokensSettingsTab.vue';
import { api } from '@/api/client';

vi.mock('@/api/client', () => {
    return {
        api: {
            organisations: {
                tokens: {
                    list: vi.fn(),
                    create: vi.fn(),
                    delete: vi.fn()
                },
                rbac: {
                    permissions: vi.fn()
                }
            }
        }
    };
});

describe('ApiTokensSettingsTab.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('fetches and displays API tokens on mount', async () => {
        const mockTokens = [
            {
                id: 1,
                name: 'Prod Token',
                permissions: ['template:get'],
                createdAt: '2026-01-01T00:00:00Z',
                expiresAt: null
            }
        ];
        const mockPerms = [{ id: 1, name: 'template:get' }];

        vi.mocked(api.organisations.tokens.list).mockResolvedValue(mockTokens);
        vi.mocked(api.organisations.rbac.permissions).mockResolvedValue(
            mockPerms
        );

        const wrapper = mount(ApiTokensSettingsTab, {
            props: {
                orgId: 1
            },
            global: {
                stubs: {
                    TokenModal: true
                }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        expect(api.organisations.tokens.list).toHaveBeenCalledWith(1);
        expect(api.organisations.rbac.permissions).toHaveBeenCalledWith(1);
    });

    it('prompts confirmation and deletes token on confirmation', async () => {
        const mockTokens = [
            {
                id: 42,
                name: 'Dev Token',
                permissions: [],
                createdAt: '2026-01-01T00:00:00Z',
                expiresAt: null
            }
        ];
        vi.mocked(api.organisations.tokens.list).mockResolvedValue(mockTokens);
        vi.mocked(api.organisations.rbac.permissions).mockResolvedValue([]);
        vi.mocked(api.organisations.tokens.delete).mockResolvedValue();

        window.confirm = vi.fn().mockReturnValue(true);
        const confirmSpy = vi.spyOn(window, 'confirm');

        const wrapper = mount(ApiTokensSettingsTab, {
            props: { orgId: 1 },
            global: {
                stubs: { TokenModal: true }
            }
        });

        await vi.runAllTimersAsync();
        await wrapper.vm.$nextTick();

        const deleteButton = wrapper.find('button[title="Delete token"]');
        await deleteButton.trigger('click');

        expect(confirmSpy).toHaveBeenCalled();
        expect(api.organisations.tokens.delete).toHaveBeenCalledWith(1, 42);
    });
});
