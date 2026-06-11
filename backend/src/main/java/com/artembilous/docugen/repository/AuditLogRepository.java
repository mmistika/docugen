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

package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
                SELECT a.logId, a.action, u.name, u.surname, u.email, a.timestamp, a.tokenName
                FROM AuditLog a
                LEFT JOIN a.user u
                WHERE a.organisation.organisationId = :orgId
                ORDER BY a.timestamp DESC
            """)
    List<Object[]> findRecentActivitiesRaw(Long orgId, Pageable pageable);

    @Query("""
        SELECT a
        FROM AuditLog a
        WHERE a.organisation.organisationId = :orgId
        AND a.timestamp >= :from
        AND a.timestamp <= :to
        ORDER BY a.timestamp DESC
    """)
    Page<AuditLog> findAll(Long orgId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}