package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.AuditLogDTO;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/org/{orgId}/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    @GetMapping
    public Page<AuditLogDTO> get(
            @AuthenticationPrincipal User user,
            @PathVariable Long orgId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            Pageable pageable
    ) {
        LocalDateTime now = LocalDateTime.now();
        if (to == null) {
            to = now;
        }
        if (from == null) {
            from = to.minusDays(30);
        }
        return service.getLogs(
                user,
                orgId,
                from,
                to,
                pageable
        );
    }
}