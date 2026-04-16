import { defineStore } from 'pinia'
import { api } from '@/api/client'
import type {Organisation} from "@/types/organisation.ts";

interface OrgState {
    organisations: Organisation[]
    currentOrgId: number | null
}

export const useOrgStore = defineStore('org', {
    state: (): OrgState => ({
        organisations: [],
        currentOrgId: null
    }),

    getters: {
        currentOrg(state): Organisation | undefined {
            return state.organisations.find(o => o.id === state.currentOrgId)
        }
    },

    actions: {
        async fetch(): Promise<void> {
            const data = await api.organisations.my()
            this.organisations = data

            if (!this.currentOrgId && data.length > 0) {
                this.currentOrgId = data[0].id
            }
        },

        setCurrentOrg(id: number) {
            this.currentOrgId = id
        }
    }
})