package com.tech.enterprise.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.model.ProductVariant;
import com.tech.enterprise.model.Tenant;
import com.tech.enterprise.security.AdminUserDetails;
import com.tech.enterprise.service.AdminAuthService;
import com.tech.enterprise.service.ProductVariantService;
import com.tech.enterprise.tenant.TenantResolver;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{tenantSlug}/admin")
@RequiredArgsConstructor
public class AdminVariantController {

    private final ProductVariantService variantService;
    private final AdminAuthService adminAuthService;
    private final TenantResolver tenantResolver;

    /**
     * Resolve tenant from URL slug. Used for public endpoints.
     */
    private Long resolveTenantId(String tenantSlug) {
        return tenantResolver.resolveTenant(tenantSlug).getId();
    }

    private Long validateAdminTenantAccess(String tenantSlug) {
        AdminUserDetails currentAdmin = adminAuthService.getCurrentAdmin();
        if (currentAdmin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Tenant urlTenant = tenantResolver.resolveTenant(tenantSlug);

        if (!currentAdmin.getTenantId().equals(urlTenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: You can only access your own tenant's resources");
        }

        return urlTenant.getId();
    }

    /**
     * Get all variants for a specific product.
     * GET /api/{tenantSlug}/admin/products/{productId}/variants
     */
    @GetMapping("/products/{productId}/variants")
    public List<ProductVariant> getVariants(
            @PathVariable String tenantSlug,
            @PathVariable Long productId) {
        Long tenantId = resolveTenantId(tenantSlug);
        return variantService.getVariantsByProductId(productId, tenantId);
    }

    /**
     * Create a new variant for a product.
     * POST /api/{tenantSlug}/admin/products/{productId}/variants
     */
    @PostMapping("/products/{productId}/variants")
    public ProductVariant createVariant(
            @PathVariable String tenantSlug,
            @PathVariable Long productId,
            @RequestBody ProductVariant variant) {
        Long tenantId = validateAdminTenantAccess(tenantSlug);
        return variantService.saveVariant(productId, variant, tenantId);
    }

    /**
     * Update an existing variant.
     * PUT /api/{tenantSlug}/admin/variants/{variantId}
     */
    @PutMapping("/variants/{variantId}")
    public ProductVariant updateVariant(
            @PathVariable String tenantSlug,
            @PathVariable Long variantId,
            @RequestBody ProductVariant variant) {
        Long tenantId = validateAdminTenantAccess(tenantSlug);
        return variantService.updateVariant(variantId, variant, tenantId);
    }

    /**
     * Delete a variant (soft delete).
     * DELETE /api/{tenantSlug}/admin/variants/{variantId}
     */
    @DeleteMapping("/variants/{variantId}")
    public void deleteVariant(
            @PathVariable String tenantSlug,
            @PathVariable Long variantId) {
        Long tenantId = validateAdminTenantAccess(tenantSlug);
        variantService.deleteVariant(variantId, tenantId);
    }
}
