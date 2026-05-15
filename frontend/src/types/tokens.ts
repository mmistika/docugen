export interface ApiTokenDTO {
    id: number;
    name: string;
    createdAt: string;
    expiresAt: string | null;
    permissions: string[];
}

export interface CreateApiTokenRequest {
    name: string;
    permissions: string[];
    expiresAt: string | null;
}

export interface CreateApiTokenResponse {
    id: number;
    name: string;
    rawToken: string;
    createdAt: string;
    expiresAt: string | null;
    permissions: string[];
}
