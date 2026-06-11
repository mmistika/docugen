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

import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import TokenModal from '../TokenModal.vue';
import PermissionSelector from '@/components/common/PermissionSelector.vue';
import type { PermissionDTO } from '@/types/rbac';

describe('TokenModal.vue', () => {
    const allPermissions: PermissionDTO[] = [
        { id: 1, name: 'document:generate' }
    ];

    it('renders generator fields when no token has been generated', () => {
        const wrapper = mount(TokenModal, {
            props: {
                show: true,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true, PermissionSelector: true }
            }
        });

        expect(wrapper.text()).toContain('Generate API Token');
        expect(
            wrapper
                .find('input[placeholder="e.g. CI/CD Document Builder"]')
                .exists()
        ).toBe(true);
    });

    it('validates name and permissions and emits save', async () => {
        const wrapper = mount(TokenModal, {
            props: {
                show: true,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        const generateBtn = wrapper
            .findAll('button')
            .filter((b) => b.text().includes('Generate Token'))[0];
        await generateBtn.trigger('click');

        expect(wrapper.text()).toContain('Token name is required.');
        expect(wrapper.emitted('save')).toBeFalsy();

        const input = wrapper.find(
            'input[placeholder="e.g. CI/CD Document Builder"]'
        );
        await input.setValue('Test App');

        const selector = wrapper.findComponent(PermissionSelector);
        const label = selector.find('label');
        await label.find('div').trigger('click');

        await generateBtn.trigger('click');
        expect(wrapper.emitted('save')).toBeTruthy();
        expect(wrapper.emitted('save')?.[0]?.[0]).toBe('Test App');
        expect(wrapper.emitted('save')?.[0]?.[1]).toEqual([
            'document:generate'
        ]);
    });

    it('renders token details when exposed method setGeneratedToken is called', async () => {
        const wrapper = mount(TokenModal, {
            props: {
                show: true,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        (wrapper.vm as any).setGeneratedToken('my-secret-token');
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Token Generated');
        expect(wrapper.text()).toContain(
            'Make sure to copy your API token now.'
        );
        expect(wrapper.find('input[readonly]').element.value).toBe(
            'my-secret-token'
        );
    });

    it('copies generated token to clipboard when copy button is clicked', async () => {
        const writeTextMock = vi.fn().mockResolvedValue(undefined);
        vi.stubGlobal('navigator', {
            clipboard: {
                writeText: writeTextMock
            }
        });

        const wrapper = mount(TokenModal, {
            props: {
                show: true,
                allPermissions,
                isSaving: false
            },
            global: {
                stubs: { Teleport: true }
            }
        });

        (wrapper.vm as any).setGeneratedToken('my-secret-token');
        await wrapper.vm.$nextTick();

        const copyBtn = wrapper.find('button[title="Copy to Clipboard"]');
        await copyBtn.trigger('click');

        expect(writeTextMock).toHaveBeenCalledWith('my-secret-token');
    });
});
