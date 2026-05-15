package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.DailyUsageDTO;
import com.artembilous.docugen.dto.DashboardDataDTO;
import com.artembilous.docugen.dto.SimplifiedAuditLogDTO;
import com.artembilous.docugen.entity.AuditAction;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.AuditLogRepository;
import com.artembilous.docugen.repository.DocumentRepository;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.TemplateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MembershipRepository membershipRepository;
    private final TemplateRepository templateRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;

    @PreAuthorize("hasPermission(#orgId, 'statistics:view')")
    @Transactional
    public DashboardDataDTO getDashboardData(User user, Long orgId) {
        long totalUsers = membershipRepository.countByOrganisationOrganisationId(orgId);
        long totalTemplates = templateRepository.countByOrganisationOrganisationId(orgId);
        long totalDocuments = documentRepository.countByOrganisationOrganisationId(orgId);

        List<SimplifiedAuditLogDTO> recentActivity = auditLogRepository.findRecentActivitiesRaw(orgId, PageRequest.of(0, 5))
                .stream()
                .map(row -> {
                    String name = (String) row[2];
                    String surname = (String) row[3];
                    String email = (String) row[4];
                    String tokenName = (String) row[6];
                    boolean isToken = tokenName != null;
                    String fullName = name != null
                            ? name + (surname != null ? " " + surname : "")
                            : (isToken ? tokenName : "System");
                    return new SimplifiedAuditLogDTO(
                            (Long) row[0],
                            ((AuditAction) row[1]).name(),
                            fullName,
                            email,
                            isToken,
                            (LocalDateTime) row[5]
                    );
                })
                .collect(Collectors.toList());

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<DailyUsageDTO> usageTrends = documentRepository.findDailyUsageTrendsRaw(orgId, since)
                .stream()
                .map(row -> new DailyUsageDTO(
                        row[0].toString(),
                        (Long) row[1]
                ))
                .collect(Collectors.toList());

        return new DashboardDataDTO(
                totalUsers,
                totalTemplates,
                totalDocuments,
                recentActivity,
                usageTrends
        );
    }
}
