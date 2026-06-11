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