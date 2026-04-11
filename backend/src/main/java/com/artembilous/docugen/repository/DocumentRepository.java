package com.artembilous.docugen.repository;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.entity.Document;
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
    List<DocumentDTO> findAllByOrg(Long orgId);

    Optional<Document> findByDocumentIdAndOrganisationOrganisationId(Long id, Long orgId);
}
