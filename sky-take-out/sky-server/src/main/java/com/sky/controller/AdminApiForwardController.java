package com.sky.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Keeps the /api prefix expected by the prebuilt admin UI while preserving
 * the existing /admin controller mappings and JWT interceptor rules.
 */
@Controller
public class AdminApiForwardController {

    private static final String LEGACY_API_PREFIX = "/api";
    private static final String ADMIN_API_PREFIX = "/admin";

    @RequestMapping("/api/**")
    public void forwardToAdminApi(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        String adminPath = ADMIN_API_PREFIX + requestPath.substring(LEGACY_API_PREFIX.length());
        request.getRequestDispatcher(adminPath).forward(request, response);
    }
}
