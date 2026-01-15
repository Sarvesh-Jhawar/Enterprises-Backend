package com.tech.enterprise.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.model.Tenant;
import com.tech.enterprise.repo.TenantRepository;

import lombok.RequiredArgsConstructor;

/**
 * Simple utility service to resolve tenant from URL slug.
 * Throws appropriate HTTP exceptions for invalid/inactive tenants.
 */
@Service
@RequiredArgsConstructor
public class TenantResolver {

    private final TenantRepository tenantRepository;

    /**
     * Resolves tenant from URL slug.
     * 
     * @param slug The tenant slug from the URL path
     * @return The active Tenant entity
     * @throws ResponseStatusException 404 if tenant not found, 403 if inactive
     */
    public Tenant resolveTenant(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tenant not found"));

        if (!Boolean.TRUE.equals(tenant.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Tenant is inactive");
        }

        return tenant;
    }
}
