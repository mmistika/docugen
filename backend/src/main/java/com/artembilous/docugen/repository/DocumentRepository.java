package com.artembilous.docugen.repository;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
        SELECT new com.artembilous.docugen.dto.DocumentDTO(
            d.documentId,
            d.name,
            d.status,
            t.name,
            d.createdAt
        )
        FROM Document d
        JOIN d.templateVersion tv
        JOIN tv.template t
        WHERE d.organisation.organisationId = :orgId
        ORDER BY d.createdAt DESC
    """)
    Page<DocumentDTO> findAllByOrg(Long orgId, Pageable pageable);

    Optional<Document> findByDocumentIdAndOrganisationOrganisationId(Long id, Long orgId);

    long countByOrganisationOrganisationId(Long orgId);

    @Query("""
                SELECT CAST(d.createdAt AS LocalDate), COUNT(d)
                FROM Document d
                WHERE d.organisation.organisationId = :orgId
                  AND d.createdAt >= :since
                GROUP BY CAST(d.createdAt AS LocalDate)
                ORDER BY CAST(d.createdAt AS LocalDate) ASC
            """)
    List<Object[]> findDailyUsageTrendsRaw(Long orgId, java.time.LocalDateTime since);
}
