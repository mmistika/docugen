export interface DailyUsageDTO {
    date: string;
    count: number;
}

export interface SimplifiedAuditLogDTO {
    id: number;
    action: string;
    userName: string | null;
    userEmail: string;
    timestamp: string;
}

export interface DashboardDataDTO {
    totalUsers: number;
    totalTemplates: number;
    totalDocuments: number;
    recentActivity: SimplifiedAuditLogDTO[];
    usageTrends: DailyUsageDTO[];
}
