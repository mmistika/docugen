export interface PermissionDTO {
    id: number;
    name: string;
}

export interface RoleDTO {
    name: string;
    permissions: string[];
}

export interface RoleUpdateRequest {
    name: string;
    permissions: string[];
}
