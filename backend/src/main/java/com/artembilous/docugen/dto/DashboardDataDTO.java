package com.artembilous.docugen.dto;

import java.util.List;

public record DashboardDataDTO(
        long totalUsers,
        long totalTemplates,
        long totalDocuments,
        List<SimplifiedAuditLogDTO> recentActivity,
        List<DailyUsageDTO> usageTrends
) {
}
