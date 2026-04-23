import axios, {type AxiosInstance} from 'axios'
import {auth0} from '@/main'
import type {MeResponse} from "@/types/user.ts"
import type {Member} from "@/types/member.ts";
import type {Organisation} from "@/types/organisation.ts";
import type {Template, TemplateDetail} from "@/types/template.ts";
import type {Document} from "@/types/document.ts";

const apiInstance: AxiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api'
})

apiInstance.interceptors.request.use(async (config) => {
    try {
        const token = await auth0.getAccessTokenSilently()
        if (config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }
    } catch (e) {
        console.error("Auth0 token fetch failed", e)
    }
    return config
})

apiInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        return Promise.reject(error)
    }
)

const users = {
    async me(): Promise<MeResponse> {
        const res = await apiInstance.get<MeResponse>('/users/me')
        return res.data
    },

    async completeRegistration(data: { name: string; surname: string }): Promise<void> {
        await apiInstance.post('/users/complete-registration', data)
    }
}

const organisations = {
    async create(data: { name: string }): Promise<void> {
        await apiInstance.post('/org', data)
    },
    async my(): Promise<Organisation[]> {
        const res = await apiInstance.get('/org/my')
        return res.data
    },
    async members(orgId: number): Promise<Member[]> {
        const res = await apiInstance.get(`/org/${orgId}/members`)
        return res.data
    },
    templates: {
        async all(orgId: number): Promise<Template[]> {
            const res = await apiInstance.get(`/org/${orgId}/templates`)
            return res.data
        },
        async getForEdit(orgId: number, id: number): Promise<TemplateDetail> {
            const res = await apiInstance.get(`/org/${orgId}/templates/${id}/edit`)
            return res.data
        },
        async create(orgId: number, data: {
            name: string;
            manifest: string;
            content: string;
        }): Promise<number> {
            const res = await apiInstance.post(`/org/${orgId}/templates`, data)
            return res.data
        },
        async update(orgId: number, id: number, data: {
            name: string;
            manifest: string;
            content: string;
        }): Promise<number> {
            const res = await apiInstance.put(`/org/${orgId}/templates/${id}`, data)
            return res.data
        },
        async publish(orgId: number, id: number): Promise<void> {
            await apiInstance.post(`/org/${orgId}/templates/${id}/publish`)
        }
    },
    documents: {
        async all(orgId: number): Promise<Document[]> {
            const res = await apiInstance.get(`/org/${orgId}/documents`)
            return res.data
        },
        async view(orgId: number, id: number): Promise<Blob> {
            const res = await apiInstance.get(
                `/org/${orgId}/documents/${id}`,
                { responseType: 'blob' }
            )
            return res.data
        },
        async finalise(orgId: number, id: number): Promise<void> {
            await apiInstance.put(`/org/${orgId}/documents/${id}/finalise`)
        },
        async revertToDraft(orgId: number, id: number): Promise<void> {
            await apiInstance.put(`/org/${orgId}/documents/${id}/revert`)
        }
    }
}

export const api = {
    users,
    organisations,
}