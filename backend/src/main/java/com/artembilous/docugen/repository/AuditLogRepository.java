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