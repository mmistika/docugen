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

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.dto.TemplateUpdateRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/{orgId}/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService service;

    @GetMapping
    public List<TemplateDTO> all(@AuthenticationPrincipal User user, @PathVariable Long orgId) {
        return service.getAll(user, orgId);
    }

    @GetMapping("/{id}/edit")
    public TemplateDetailDTO getForEdit(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        return service.getForUpdate(user, orgId, id);
    }

    @GetMapping("/{id}/active")
    public TemplateDetailDTO getActive(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        return service.getActive(user, orgId, id);
    }

    @PostMapping
    public Long create(@AuthenticationPrincipal User user, @PathVariable Long orgId, @RequestBody TemplateUpdateRequest req) {
        return service.update(user, orgId, null, req);
    }

    @PutMapping("/{id}")
    public Long update(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id, @RequestBody TemplateUpdateRequest req) {
        return service.update(user, orgId, id, req);
    }

    @PostMapping("/{id}/publish")
    public void publish(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable Long id) {
        service.publish(user, orgId, id);
    }
}