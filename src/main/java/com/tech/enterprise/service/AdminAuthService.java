package com.tech.enterprise.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.dto.LoginResponse;
import com.tech.enterprise.model.Tenant;
import com.tech.enterprise.security.AdminUserDetails;
import com.tech.enterprise.security.AdminUserDetailsService;
import com.tech.enterprise.tenant.TenantResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;

/**
 * Authentication service handling admin login with tenant-aware authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    private final TenantResolver tenantResolver;
    private final AdminUserDetailsService adminUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate admin and establish session.
     * 
     * @param tenantSlug The tenant slug from URL path
     * @param username   The admin's username
     * @param password   The admin's plain text password
     * @param request    The HTTP request for session management
     * @return LoginResponse with admin and tenant details
     * @throws ResponseStatusException 404 if tenant not found, 403 if inactive, 401
     *                                 if invalid credentials
     */
    public LoginResponse login(String tenantSlug, String username, String password,
            HttpServletRequest request, HttpServletResponse response) {

        // 1. Resolve tenant from slug (throws 404/403 if invalid)
        Tenant tenant = tenantResolver.resolveTenant(tenantSlug);

        // 2. Load admin by username and tenant
        AdminUserDetails adminDetails;
        try {
            adminDetails = adminUserDetailsService
                    .loadAdminByUsernameAndTenant(username, tenant.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 3. Verify password using BCrypt
        if (!passwordEncoder.matches(password, adminDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 4. Create authentication token and set in SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                adminDetails,
                null,
                adminDetails.getAuthorities());
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);

        // 5. Explicitly save SecurityContext in Repository for Spring Security 6
        securityContextRepository.saveContext(context, request, response);

        log.info("Admin {} successfully logged in for tenant {}", username, tenantSlug);

        // 6. Return success response
        return LoginResponse.builder()
                .adminId(adminDetails.getAdminId())
                .username(adminDetails.getUsername())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .tenantSlug(tenant.getSlug())
                .message("Login successful")
                .build();
    }

    /**
     * Logout admin and invalidate session.
     * 
     * @param request The HTTP request for session management
     */
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * Get the currently authenticated admin's details.
     * 
     * @return AdminUserDetails or null if not authenticated
     */
    public AdminUserDetails getCurrentAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails) {
            return (AdminUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the tenant ID of the currently authenticated admin.
     * 
     * @return The tenant ID
     * @throws ResponseStatusException 401 if not authenticated
     */
    public Long getCurrentTenantId() {
        AdminUserDetails admin = getCurrentAdmin();
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return admin.getTenantId();
    }
}
