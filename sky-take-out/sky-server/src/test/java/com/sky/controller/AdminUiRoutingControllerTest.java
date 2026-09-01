package com.sky.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminUiRoutingControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AdminSpaController(), new AdminApiForwardController()).build();
    }

    @Test
    void rootRedirectsToPackagedAdminUi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(redirectedUrl("/admin-ui/index.html"));
    }

    @Test
    void legacyAdminApiPathForwardsToExistingControllerPath() throws Exception {
        mockMvc.perform(post("/api/employee/login"))
                .andExpect(forwardedUrl("/admin/employee/login"));
    }
}
