import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { auth0 } from '@/main'

const routes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/components/Layout.vue'),
        redirect: '/organisations',
        children: [
            {
                path: 'profile',
                name: 'profile',
                component: () => import('@/views/ProfileView.vue'),
            },
            {
                path: 'organisations',
                name: 'organisations',
                component: () => import('@/views/OrganisationsView.vue')
            },
            // {
            //     path: 'dashboard',
            //     name: 'dashboard',
            //     component: () => import('@/views/DashboardView.vue')
            // },
            {
                path: 'members',
                name: 'members',
                component: () => import('@/views/MembersView.vue'),
            },
            {
                path: 'templates',
                name: 'templates',
                component: () => import('@/views/TemplatesView.vue'),
            },
            {
                path: 'templates/new',
                name: 'template-new',
                component: () => import('@/views/TemplateEditorView.vue'),
            },
            {
                path: 'templates/:id',
                name: 'template-id',
                component: () => import('@/views/TemplateEditorView.vue'),
            }
        ]
    },
    {
        path: '/complete-profile',
        name: 'complete-profile',
        component: () => import('@/views/RegistrationWizardStep1.vue'),
        meta: { isWizard: true }
    },
    {
        path: '/create-first-org',
        name: 'create-first-org',
        component: () => import('@/views/RegistrationWizardStep2.vue'),
        meta: { isWizard: true }
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'not-found',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(async (to) => {
    while (auth0.isLoading.value) {
        await new Promise(resolve => setTimeout(resolve, 50))
    }

    if (!auth0.isAuthenticated.value) {
        await auth0.loginWithRedirect({appState: {target: to.fullPath}})
        return false
    }

    try {
        const authStore = useAuthStore()

        if (!authStore.user) {
            await authStore.fetchMe()
        }

        const user = authStore.user
        if (!user) return false

        if (!user.name || !user.surname) {
            if (to.name !== 'complete-profile') {
                return { name: 'complete-profile' }
            }
            return true
        }

        if (!user.registered) {
            if (to.name !== 'create-first-org') {
                return { name: 'create-first-org' }
            }
            return true
        }

        if (to.meta.isWizard) {
            return { name: 'dashboard' }
        }

        return true
    } catch (error) {
        return false
    }
})

export default router