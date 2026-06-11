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
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<DocumentDTO> list(@AuthenticationPrincipal User user, @PathVariable Long orgId, Pageable pageable) {
        return service.list(user, orgId, pageable);
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