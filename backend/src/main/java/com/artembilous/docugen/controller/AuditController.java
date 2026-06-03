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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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
            Instant from,

            @RequestParam(required = false)
            Instant to,

            Pageable pageable
    ) {
        if (to == null) {
            to = Instant.now();
        }
        if (from == null) {
            from = to.minus(30, ChronoUnit.DAYS);
        }

        LocalDateTime fromLocal = LocalDateTime.ofInstant(from, ZoneOffset.UTC);
        LocalDateTime toLocal = LocalDateTime.ofInstant(to, ZoneOffset.UTC);

        return service.getLogs(
                user,
                orgId,
                fromLocal,
                toLocal,
                pageable
        );
    }
}