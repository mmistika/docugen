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