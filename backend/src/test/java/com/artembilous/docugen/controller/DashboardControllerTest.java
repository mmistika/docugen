package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DashboardDataDTO;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class, properties = "cors.allowed-origins=http://localhost")
class DashboardControllerTest extends BaseControllerTest {

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void get_ShouldReturnDashboardData() throws Exception {
        DashboardDataDTO dto = new DashboardDataDTO(5L, 12L, 120L, List.of(), List.of());

        when(dashboardService.getDashboardData(any(User.class), eq(10L))).thenReturn(dto);

        mockMvc.perform(get("/api/org/10/dashboard")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(5))
                .andExpect(jsonPath("$.totalTemplates").value(12))
                .andExpect(jsonPath("$.totalDocuments").value(120));
    }
}
