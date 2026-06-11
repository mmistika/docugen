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

import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import DocumentCanvas from '../DocumentCanvas.vue';

// Mock ResizeObserver
const observeMock = vi.fn();
const disconnectMock = vi.fn();
class MockResizeObserver {
    observe = observeMock;
    unobserve = vi.fn();
    disconnect = disconnectMock;
}
vi.stubGlobal('ResizeObserver', MockResizeObserver);

describe('DocumentCanvas.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders slot content', () => {
        const wrapper = mount(DocumentCanvas, {
            slots: {
                default: '<div id="test-content">Document Text</div>'
            }
        });
        expect(wrapper.find('#test-content').exists()).toBe(true);
        expect(wrapper.text()).toContain('Document Text');
    });

    it('sets the initial scale based on props or default', () => {
        const defaultWrapper = mount(DocumentCanvas);
        expect(defaultWrapper.text()).toContain('85%');

        const customWrapper = mount(DocumentCanvas, {
            props: {
                initialScale: 0.5
            }
        });
        expect(customWrapper.text()).toContain('50%');
    });

    it('zooms in when the zoom in button is clicked', async () => {
        const wrapper = mount(DocumentCanvas, {
            props: { initialScale: 0.85 }
        });
        const zoomInButton = wrapper.find('button[title="Zoom in"]');
        await zoomInButton.trigger('click');
        expect(wrapper.text()).toContain('95%');
    });

    it('zooms out when the zoom out button is clicked', async () => {
        const wrapper = mount(DocumentCanvas, {
            props: { initialScale: 0.85 }
        });
        const zoomOutButton = wrapper.find('button[title="Zoom out"]');
        await zoomOutButton.trigger('click');
        expect(wrapper.text()).toContain('75%');
    });

    it('respects MAX_SCALE and MIN_SCALE boundaries', async () => {
        const wrapper = mount(DocumentCanvas, {
            props: { initialScale: 1.95 }
        });
        const zoomInButton = wrapper.find('button[title="Zoom in"]');

        await zoomInButton.trigger('click'); // to 2.0
        expect(wrapper.text()).toContain('200%');

        await zoomInButton.trigger('click');
        expect(wrapper.text()).toContain('200%');

        // Zoom out bounds
        const wrapperMin = mount(DocumentCanvas, {
            props: { initialScale: 0.35 }
        });
        const zoomOutButton = wrapperMin.find('button[title="Zoom out"]');

        await zoomOutButton.trigger('click'); // to 0.3
        expect(wrapperMin.text()).toContain('30%');

        await zoomOutButton.trigger('click');
        expect(wrapperMin.text()).toContain('30%');
    });

    it('initializes and disconnects ResizeObserver correctly', () => {
        const wrapper = mount(DocumentCanvas);
        expect(observeMock).toHaveBeenCalled();

        wrapper.unmount();
        expect(disconnectMock).toHaveBeenCalled();
    });
});
