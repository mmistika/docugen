import { defineStore } from 'pinia'
import { api } from '@/api/client'
import { auth0 } from '@/main'
import type { MeResponse } from '@/types/user'

interface AuthState {
    user: MeResponse | null
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        user: null
    }),
    actions: {
        async fetchMe() {
            this.user = await api.users.me()
        },
        async completeProfile(name: string, surname: string) {
            await api.users.completeRegistration({ name, surname })
            if (this.user) {
                this.user.name = name
                this.user.surname = surname
            }
        },
        async completeOrg(orgName: string) {
            await api.organisations.create({ name: orgName })
            if (this.user) {
                this.user.registered = true
            }
        },
        async logout() {
            await auth0.logout({ logoutParams: { returnTo: window.location.origin } })
        }
    }
})