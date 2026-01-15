package com.tech.enterprise.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tech.enterprise.model.Admin;

/**
 * Custom UserDetails implementation wrapping the Admin entity.
 * Exposes tenant information for tenant-scoped operations.
 */
public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    /**
     * Get the tenant ID associated with this admin.
     * Used for enforcing tenant isolation in service layer.
     */
    public Long getTenantId() {
        return admin.getTenantId();
    }

    /**
     * Get the admin's database ID.
     */
    public Long getAdminId() {
        return admin.getId();
    }

    /**
     * Get the underlying Admin entity.
     */
    public Admin getAdmin() {
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Simple role - no complex permissions for now
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(admin.getActive());
    }
}
