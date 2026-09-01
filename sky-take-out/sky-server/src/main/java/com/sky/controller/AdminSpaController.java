package com.sky.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * Exposes the prebuilt admin application from the Spring Boot process.
 */
@Controller
public class AdminSpaController {

    @GetMapping({"/", "/admin-ui", "/admin-ui/"})
    public void adminHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin-ui/index.html");
    }
}
