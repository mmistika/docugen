export interface DailyUsageDTO {
    date: string;
    count: number;
}

export interface SimplifiedAuditLogDTO {
    id: number;
    action: string;
    userName: string | null;
    userEmail: string | null;
    isToken: boolean;
    timestamp: string;
}

export interface DashboardDataDTO {
    totalUsers: number;
    totalTemplates: number;
    totalDocuments: number;
    recentActivity: SimplifiedAuditLogDTO[];
    usageTrends: DailyUsageDTO[];
}
