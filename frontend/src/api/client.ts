import axios, { type AxiosInstance } from 'axios';
import { auth0 } from '@/auth';
import type { MeResponse } from '@/types/user.ts';
import type { Member } from '@/types/member.ts';
import type { Organisation } from '@/types/organisation.ts';
import type { Template, TemplateDetail } from '@/types/template.ts';
import type { Document } from '@/types/document.ts';
import type {
    PermissionDTO,
    RoleDTO,
    RoleUpdateRequest
} from '@/types/rbac.ts';
import type { AuditLogDTO } from '@/types/audit.ts';
import type { DashboardDataDTO } from '@/types/dashboard.ts';
import type {
    ApiTokenDTO,
    CreateApiTokenRequest,
    CreateApiTokenResponse
} from '@/types/tokens.ts';
import { useNotificationStore } from '@/stores/notification';

const apiInstance: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL
});

apiInstance.interceptors.request.use(async (config) => {
    try {
        const token = await auth0.getAccessTokenSilently();
        if (config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    } catch (e) {
        console.error('Auth0 token fetch failed', e);
    }
    return config;
});

apiInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        const notificationStore = useNotificationStore();
        let message = 'An unexpected error occurred.';

        if (error.response) {
            const status = error.response.status;
            const data = error.response.data;

            if (status === 401) {
                message = 'Session expired. Please log in again.';
            } else if (status === 403) {
                message = 'You do not have permission to perform this action.';
            } else if (status === 404) {
                message = 'Requested resource not found.';
            } else if (data && typeof data.message === 'string') {
                message = data.message;
            } else if (data && typeof data === 'string') {
                message = data;
            } else {
                message = `Request failed with status code ${status}.`;
            }
        } else if (error.request) {
            message = 'No response from server. Please check your connection.';
        } else if (error.message) {
            message = error.message;
        }

        notificationStore.error(message);
        return Promise.reject(error);
    }
);

const users = {
    async me(): Promise<MeResponse> {
        const res = await apiInstance.get<MeResponse>('/users/me');
        return res.data;
    },
    async updateProfile(data: {
        name: string;
        surname: string;
        image: string | null;
    }): Promise<MeResponse> {
        const res = await apiInstance.put<MeResponse>('/users/profile', data);
        return res.data;
    },
    async completeRegistration(data: {
        name: string;
        surname: string;
        image?: string | null;
    }): Promise<void> {
        await apiInstance.post('/users/complete-registration', data);
    },
    async invite(data: {
        orgId: number;
        email: string;
        role: string;
    }): Promise<void> {
        await apiInstance.post('/users/invite', data);
    }
};

const organisations = {
    async create(data: { name: string }): Promise<void> {
        await apiInstance.post('/org', data);
    },
    async my(): Promise<Organisation[]> {
        const res = await apiInstance.get('/org/my');
        return res.data;
    },
    async rename(orgId: number, data: { name: string }) {
        await apiInstance.patch(`/org/${orgId}`, data);
    },
    members: {
        async all(orgId: number): Promise<Member[]> {
            const res = await apiInstance.get(`/org/${orgId}/members`);
            return res.data;
        },
        async updateRoles(
            orgId: number,
            memberId: number,
            roles: string[]
        ): Promise<void> {
            await apiInstance.put(`/org/${orgId}/members/${memberId}/roles`, {
                roles
            });
        }
    },
    rbac: {
        async permissions(orgId: number): Promise<PermissionDTO[]> {
            const res = await apiInstance.get(`/org/${orgId}/rbac/permissions`);
            return res.data;
        },
        async roles(orgId: number): Promise<RoleDTO[]> {
            const res = await apiInstance.get(`/org/${orgId}/rbac/roles`);
            return res.data;
        },
        async createRole(orgId: number, req: RoleUpdateRequest): Promise<void> {
            await apiInstance.post(`/org/${orgId}/rbac/roles`, req);
        },
        async updateRole(orgId: number, req: RoleUpdateRequest): Promise<void> {
            await apiInstance.put(`/org/${orgId}/rbac/roles`, req);
        },
        async deleteRole(orgId: number, roleName: string): Promise<void> {
            await apiInstance.delete(`/org/${orgId}/rbac/roles/${roleName}`);
        }
    },
    tokens: {
        async list(orgId: number): Promise<ApiTokenDTO[]> {
            const res = await apiInstance.get(`/org/${orgId}/settings/tokens`);
            return res.data;
        },
        async create(
            orgId: number,
            req: CreateApiTokenRequest
        ): Promise<CreateApiTokenResponse> {
            const res = await apiInstance.post(
                `/org/${orgId}/settings/tokens`,
                req
            );
            return res.data;
        },
        async delete(orgId: number, tokenId: number): Promise<void> {
            await apiInstance.delete(
                `/org/${orgId}/settings/tokens/${tokenId}`
            );
        }
    },
    templates: {
        async all(orgId: number): Promise<Template[]> {
            const res = await apiInstance.get(`/org/${orgId}/templates`);
            return res.data;
        },
        async getForEdit(orgId: number, id: number): Promise<TemplateDetail> {
            const res = await apiInstance.get(
                `/org/${orgId}/templates/${id}/edit`
            );
            return res.data;
        },
        async create(
            orgId: number,
            data: {
                name: string;
                manifest: string;
                content: string;
            }
        ): Promise<number> {
            const res = await apiInstance.post(`/org/${orgId}/templates`, data);
            return res.data;
        },
        async update(
            orgId: number,
            id: number,
            data: {
                name: string;
                manifest: string;
                content: string;
            }
        ): Promise<number> {
            const res = await apiInstance.put(
                `/org/${orgId}/templates/${id}`,
                data
            );
            return res.data;
        },
        async publish(orgId: number, id: number): Promise<void> {
            await apiInstance.post(`/org/${orgId}/templates/${id}/publish`);
        }
    },
    documents: {
        async all(orgId: number): Promise<Document[]> {
            const res = await apiInstance.get(`/org/${orgId}/documents`);
            return res.data;
        },
        async generate(
            orgId: number,
            data: {
                templateId: number;
                name: string;
                data: Record<string, string>;
            }
        ): Promise<Blob> {
            const res = await apiInstance.post(
                `/org/${orgId}/documents/generate`,
                data,
                { responseType: 'blob' }
            );
            return res.data;
        },
        async view(orgId: number, id: number): Promise<Blob> {
            const res = await apiInstance.get(`/org/${orgId}/documents/${id}`, {
                responseType: 'blob'
            });
            return res.data;
        },
        async finalise(orgId: number, id: number): Promise<void> {
            await apiInstance.patch(`/org/${orgId}/documents/${id}/finalise`);
        },
        async revertToDraft(orgId: number, id: number): Promise<void> {
            await apiInstance.patch(`/org/${orgId}/documents/${id}/revert`);
        }
    },
    audit: {
        async get(
            orgId: number,
            params: {
                page: number;
                size: number;
                from?: string;
                to?: string;
            }
        ): Promise<{
            content: AuditLogDTO[];
            page: {
                totalElements: number;
                totalPages: number;
                number: number;
                size: number;
            };
        }> {
            const res = await apiInstance.get(`/org/${orgId}/audit`, {
                params
            });
            return res.data;
        }
    },
    dashboard: {
        async get(orgId: number): Promise<DashboardDataDTO> {
            const res = await apiInstance.get(`/org/${orgId}/dashboard`);
            return res.data;
        }
    }
};

export const api = {
    users,
    organisations
};
