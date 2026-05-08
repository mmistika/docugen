export interface AuditLogDTO {
    id: number;
    action: string;
    entityType: string;
    entityId: number;
    userId: number;
    userEmail: string;
    userName: string;
    metadata: string;
    timestamp: string;
}
