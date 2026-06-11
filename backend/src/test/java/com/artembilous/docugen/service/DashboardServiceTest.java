/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.DashboardDataDTO;
import com.artembilous.docugen.entity.AuditAction;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.AuditLogRepository;
import com.artembilous.docugen.repository.DocumentRepository;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
    }

    @Test
    void getDashboardData_ShouldAggregateAndReturnDashboardDto() {
        when(membershipRepository.countByOrganisationOrganisationId(10L)).thenReturn(5L);
        when(templateRepository.countByOrganisationOrganisationId(10L)).thenReturn(12L);
        when(documentRepository.countByOrganisationOrganisationId(10L)).thenReturn(120L);

        // Mock recent activity raw row
        // row: logId, action, name, surname, email, timestamp, tokenName
        Object[] rawActivityRow = new Object[]{
                1L,
                AuditAction.DOCUMENT_GENERATED,
                "John",
                "Doe",
                "john@example.com",
                LocalDateTime.now(),
                null
        };
        when(auditLogRepository.findRecentActivitiesRaw(eq(10L), eq(PageRequest.of(0, 5))))
                .thenReturn(List.<Object[]>of(rawActivityRow));

        // Mock usage trends raw row
        // row: date, count
        Object[] rawUsageRow = new Object[]{
                "2026-06-03",
                15L
        };
        when(documentRepository.findDailyUsageTrendsRaw(eq(10L), any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(rawUsageRow));

        DashboardDataDTO dto = dashboardService.getDashboardData(testUser, 10L);

        assertNotNull(dto);
        assertEquals(5L, dto.totalUsers());
        assertEquals(12L, dto.totalTemplates());
        assertEquals(120L, dto.totalDocuments());
        assertEquals(1, dto.recentActivity().size());
        assertEquals("John Doe", dto.recentActivity().getFirst().userName());
        assertFalse(dto.recentActivity().getFirst().isToken());
        assertEquals(1, dto.usageTrends().size());
        assertEquals("2026-06-03", dto.usageTrends().getFirst().date());
        assertEquals(15L, dto.usageTrends().getFirst().count());
    }
}
