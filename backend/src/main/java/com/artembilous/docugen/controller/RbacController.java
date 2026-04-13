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