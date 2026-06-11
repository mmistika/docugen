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

import com.artembilous.docugen.dto.PermissionDTO;
import com.artembilous.docugen.dto.RoleDTO;
import com.artembilous.docugen.dto.RoleUpdateRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/org/{orgId}/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService service;

    @GetMapping("/permissions")
    public Set<PermissionDTO> permissions(@PathVariable String orgId) {
        return service.getPermissions();
    }

    @GetMapping("/roles")
    public Set<RoleDTO> roles(@AuthenticationPrincipal User user, @PathVariable Long orgId) {
        return service.getRoles(user, orgId);
    }

    @PostMapping("/roles")
    public void create(@AuthenticationPrincipal User user, @PathVariable Long orgId, @Valid @RequestBody RoleUpdateRequest req) {
        service.createRole(user, orgId, req);
    }

    @PutMapping("/roles")
    public void update(@AuthenticationPrincipal User user, @PathVariable Long orgId, @Valid @RequestBody RoleUpdateRequest req) {
        service.updateRole(user, orgId, req);
    }

    @DeleteMapping("/roles/{roleName}")
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long orgId, @PathVariable String roleName) {
        service.deleteRole(user, orgId, roleName);
    }
}