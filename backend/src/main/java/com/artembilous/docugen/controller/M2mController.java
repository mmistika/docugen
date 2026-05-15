package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.service.DocumentService;
import com.artembilous.docugen.service.TemplateService;
import lombok.RequiredArgsConstructor;
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
        return documentService.list(null, orgId);
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
