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