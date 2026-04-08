package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.TemplateVersion;
import com.artembilous.docugen.entity.TemplateVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

    @Query("""
        SELECT tv
        FROM TemplateVersion tv
        WHERE tv.template.templateId = :templateId
        ORDER BY tv.version DESC
        LIMIT 1
    """)
    Optional<TemplateVersion> findLatest(Long templateId);

    Optional<TemplateVersion> findByTemplateTemplateIdAndStatus(Long templateId, TemplateVersionStatus status);
}