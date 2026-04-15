package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

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