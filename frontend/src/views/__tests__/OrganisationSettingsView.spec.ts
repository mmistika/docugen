import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import OrganisationSettingsView from '../OrganisationSettingsView.vue';

const routeParams = { id: '4' };
const routerPushSpy = vi.fn();
vi.mock('vue-router', () => ({
    useRoute: () => ({
        params: routeParams,
        name: 'org-settings'
    }),
    useRouter: () => ({
        push: routerPushSpy
    }),
    RouterLink: {
        template: '<a><slot /></a>'
    }
}));

describe('OrganisationSettingsView.vue', () => {
    beforeEach(() => {
        const pinia = createPinia();
        setActivePinia(pinia);
        vi.clearAllMocks();
    });

    it('renders the tabs list and displays general settings by default', () => {
        const wrapper = mount(OrganisationSettingsView, {
            global: {
                stubs: {
                    TabHeader: true,
                    GeneralSettingsTab: {
                        template: '<div class="general-mock">General</div>'
                    },
                    RbacSettingsTab: true,
                    ApiTokensSettingsTab: true
                }
            }
        });

        expect(wrapper.text()).toContain('General');
        expect(wrapper.text()).toContain('Roles & Permissions');
        expect(wrapper.text()).toContain('API Tokens');
        expect(wrapper.find('.general-mock').exists()).toBe(true);
    });

    it('changes tabs and displays correct subcomponents on tab click', async () => {
        const wrapper = mount(OrganisationSettingsView, {
            global: {
                stubs: {
                    TabHeader: true,
                    GeneralSettingsTab: true,
                    RbacSettingsTab: {
                        template: '<div class="rbac-mock">Rbac</div>'
                    },
                    ApiTokensSettingsTab: true
                }
            }
        });

        const rbacTabButton = wrapper
            .findAll('nav button')
            .find((b) => b.text().includes('Roles & Permissions'))!;
        await rbacTabButton.trigger('click');

        expect(wrapper.find('.rbac-mock').exists()).toBe(true);
    });
});
