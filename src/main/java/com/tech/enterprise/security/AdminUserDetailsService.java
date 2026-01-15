package com.tech.enterprise.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tech.enterprise.model.Admin;
import com.tech.enterprise.repo.AdminRepository;

import lombok.RequiredArgsConstructor;

/**
 * Custom UserDetailsService that loads admin by username and tenant.
 * 
 * Note: This service requires both username AND tenantId to load an admin.
 * The standard loadUserByUsername method is not used directly by our auth flow.
 * Instead, we use loadAdminByUsernameAndTenant for tenant-aware authentication.
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    /**
     * Standard UserDetailsService method - not used in our tenant-aware flow.
     * Throws exception as we require tenant context for authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException(
                "Direct username lookup not supported. Use loadAdminByUsernameAndTenant instead.");
    }

    /**
     * Load admin by username and tenant ID.
     * This is the primary method used for tenant-aware authentication.
     * 
     * @param username The admin's username
     * @param tenantId The tenant ID from the resolved tenant
     * @return AdminUserDetails wrapping the Admin entity
     * @throws UsernameNotFoundException if admin not found or inactive
     */
    public AdminUserDetails loadAdminByUsernameAndTenant(String username, Long tenantId)
            throws UsernameNotFoundException {

        Admin admin = adminRepository.findByUsernameAndTenantIdAndActiveTrue(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return new AdminUserDetails(admin);
    }
}
