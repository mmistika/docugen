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

package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.service.DocumentService;
import com.artembilous.docugen.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/m2m/org/{orgId}")
@RequiredArgsConstructor
public class M2mController {

    private final TemplateService templateService;
    private final DocumentService documentService;

    @GetMapping("/templates")
    public List<TemplateDTO> listTemplates(@PathVariable Long orgId) {
        return templateService.getAll(null, orgId);
    }

    @GetMapping("/templates/{id}/active")
    public TemplateDetailDTO getActiveTemplate(@PathVariable Long orgId, @PathVariable Long id) {
        return templateService.getActive(null, orgId, id);
    }

    @PostMapping("/documents/generate")
    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public ResponseEntity<byte[]> generateDocument(@PathVariable Long orgId, @RequestBody GenerateDocumentRequest req) {
        byte[] file = documentService.generate(null, orgId, req);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=document.pdf")
                .body(file);
    }

    @GetMapping("/documents")
    public List<DocumentDTO> listDocuments(@PathVariable Long orgId) {
        return documentService.list(null, orgId, Pageable.unpaged()).getContent();
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long orgId, @PathVariable Long id) {
        byte[] file = documentService.view(null, orgId, id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=document.pdf")
                .body(file);
    }
}
