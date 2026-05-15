export interface AuditLogDTO {
    id: number;
    action: string;
    entityType: string;
    entityId: number;
    userId: number | null;
    userEmail: string | null;
    userName: string;
    isToken: boolean;
    metadata: string;
    timestamp: string;
}
