package com.artembilous.docugen.repository;

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    @Query("""
        SELECT new com.artembilous.docugen.dto.TemplateDTO(
            t.templateId,
            t.name
        )
        FROM Template t
        WHERE t.organisation.organisationId = :orgId
    """)
    List<TemplateDTO> findAllByOrg(Long orgId);



    Optional<Template> findByTemplateIdAndOrganisation_OrganisationId(Long id, Long orgId);

    long countByOrganisationOrganisationId(Long orgId);
}