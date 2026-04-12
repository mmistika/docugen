package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/{orgId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @GetMapping
    public List<DocumentDTO> list(@AuthenticationPrincipal User user, @PathVariable Long orgId) {
        return service.list(user, orgId);
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@AuthenticationPrincipal User user, @PathVariable Long orgId, @RequestBody GenerateDocumentRequest req) {
        byte[] file = service.generate(user, orgId, req);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=document.pdf")
                .body(file);
    }

    @PatchMapping("{id}/finalise")
    public void finalise(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        service.finalise(user, orgId, id);
    }

    @PatchMapping("{id}/revert")
    public void revert(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        service.revert(user, orgId, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        byte[] file = service.view(user, orgId, id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "inline; filename=document.pdf")
                .body(file);
    }
}