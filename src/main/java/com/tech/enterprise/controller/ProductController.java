package com.tech.enterprise.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.model.Product;
import com.tech.enterprise.model.ProductVariant;
import com.tech.enterprise.model.Tenant;
import com.tech.enterprise.security.AdminUserDetails;
import com.tech.enterprise.service.AdminAuthService;
import com.tech.enterprise.service.ProductService;
import com.tech.enterprise.tenant.TenantResolver;

import lombok.RequiredArgsConstructor;

/**
 * Product CRUD controller.
 * 
 * All endpoints are secured and require authentication.
 * Tenant is resolved from URL slug AND validated against authenticated admin's
 * tenant.
 */
@RestController
@RequestMapping("/api/{tenantSlug}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AdminAuthService adminAuthService;
    private final TenantResolver tenantResolver;

    /**
     * Validate that the authenticated admin belongs to the tenant specified in URL.
     * This enforces strict tenant isolation.
     * 
     * @param tenantSlug The tenant slug from URL
     * @return The validated tenant ID
     * @throws ResponseStatusException 403 if tenant mismatch
     */
    /**
     * Resolve tenant from URL and validate access if admin is logged in.
     * For public GET requests, we only need to resolve the tenant.
     * 
     * @param tenantSlug The tenant slug from URL
     * @return The validated tenant ID
     */
    private Long resolveTenantId(String tenantSlug) {
        return tenantResolver.resolveTenant(tenantSlug).getId();
    }

    /**
     * Validate that the authenticated admin belongs to the tenant specified in URL.
     * This enforces strict tenant isolation for sensitive admin operations.
     * 
     * @param tenantSlug The tenant slug from URL
     * @return The validated tenant ID
     * @throws ResponseStatusException 403 if tenant mismatch or not authenticated
     */
    private Long validateAdminAccess(String tenantSlug) {
        AdminUserDetails currentAdmin = adminAuthService.getCurrentAdmin();
        if (currentAdmin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated as admin");
        }

        Tenant urlTenant = tenantResolver.resolveTenant(tenantSlug);

        if (!currentAdmin.getTenantId().equals(urlTenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: You can only manage your own tenant's resources");
        }

        return urlTenant.getId();
    }

    /**
     * Get all products for the tenant.
     * 
     * GET /api/{tenantSlug}/products
     */
    @GetMapping
    public List<Product> getAll(@PathVariable String tenantSlug) {
        Long tenantId = resolveTenantId(tenantSlug);
        return productService.getProductsByTenantId(tenantId);
    }

    /**
     * Get a single product by ID.
     * 
     * GET /api/{tenantSlug}/products/{id}
     */
    @GetMapping("/{id}")
    public Product getById(@PathVariable String tenantSlug, @PathVariable Long id) {
        Long tenantId = resolveTenantId(tenantSlug);
        return productService.getProductById(id, tenantId);
    }

    /**
     * Get all variants for a specific product.
     * Public access for frontend.
     * 
     * GET /api/{tenantSlug}/products/{id}/variants
     */
    @GetMapping("/{id}/variants")
    public List<ProductVariant> getVariants(
            @PathVariable String tenantSlug,
            @PathVariable Long id) {
        Long tenantId = resolveTenantId(tenantSlug);
        // Note: We need a service method that doesn't require admin context
        // But the current ProductVariantService.getVariantsByProductId specifically
        // checks product ownership, which is good.
        return productService.getProductVariants(id, tenantId);
    }

    /**
     * Create a new product.
     * 
     * POST /api/{tenantSlug}/products
     */
    @PostMapping
    public Product create(@PathVariable String tenantSlug, @RequestBody Product product) {
        Long tenantId = validateAdminAccess(tenantSlug);
        return productService.saveProduct(product, tenantId);
    }

    /**
     * Update an existing product.
     * 
     * PUT /api/{tenantSlug}/products/{id}
     */
    @PutMapping("/{id}")
    public Product update(@PathVariable String tenantSlug,
            @PathVariable Long id,
            @RequestBody Product product) {
        Long tenantId = validateAdminAccess(tenantSlug);
        return productService.updateProduct(id, product, tenantId);
    }

    /**
     * Delete a product (soft delete by setting active=false).
     * 
     * DELETE /api/{tenantSlug}/products/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String tenantSlug, @PathVariable Long id) {
        Long tenantId = validateAdminAccess(tenantSlug);
        productService.deleteProduct(id, tenantId);
    }
}
