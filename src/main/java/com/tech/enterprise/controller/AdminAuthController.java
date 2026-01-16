package com.tech.enterprise.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.enterprise.dto.LoginRequest;
import com.tech.enterprise.dto.LoginResponse;
import com.tech.enterprise.service.AdminAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Controller for admin authentication endpoints.
 * Tenant is resolved from URL path, NOT from request body.
 */
@RestController
@RequestMapping("/api/{tenantSlug}/admins")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * Admin login endpoint.
     * 
     * POST /api/{tenantSlug}/admins/login
     * 
     * Request body: { "username": "...", "password": "..." }
     * 
     * @param tenantSlug   The tenant slug from URL
     * @param loginRequest The login credentials
     * @param request      HTTP request for session management
     * @return Login response with admin profile on success
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @PathVariable String tenantSlug,
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        LoginResponse responseData = adminAuthService.login(
                tenantSlug,
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                request,
                response);

        return ResponseEntity.ok(responseData);
    }

    /**
     * Admin logout endpoint.
     * 
     * POST /api/{tenantSlug}/admins/logout
     * 
     * @param tenantSlug The tenant slug from URL (for consistency)
     * @param request    HTTP request for session invalidation
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @PathVariable String tenantSlug,
            HttpServletRequest request) {

        adminAuthService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Get current admin profile.
     * 
     * GET /api/{tenantSlug}/admins/me
     * 
     * @param tenantSlug The tenant slug from URL
     * @return LoginResponse with admin profile
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getMe(
            @PathVariable String tenantSlug) {

        LoginResponse profile = adminAuthService.getCurrentAdminProfile();
        return ResponseEntity.ok(profile);
    }
}
