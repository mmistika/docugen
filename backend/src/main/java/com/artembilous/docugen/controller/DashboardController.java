package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DashboardDataDTO;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/org/{orgId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardDataDTO get(
            @AuthenticationPrincipal User user,
            @PathVariable Long orgId
    ) {
        return service.getDashboardData(user, orgId);
    }
}
