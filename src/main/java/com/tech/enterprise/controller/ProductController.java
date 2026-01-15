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
    private Long validateTenantAccess(String tenantSlug) {
        // Get current authenticated admin
        AdminUserDetails currentAdmin = adminAuthService.getCurrentAdmin();
        if (currentAdmin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        // Resolve tenant from URL
        Tenant urlTenant = tenantResolver.resolveTenant(tenantSlug);

        // Validate admin's tenant matches URL tenant
        if (!currentAdmin.getTenantId().equals(urlTenant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: You can only access your own tenant's resources");
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
        Long tenantId = validateTenantAccess(tenantSlug);
        return productService.getProductsByTenantId(tenantId);
    }

    /**
     * Get a single product by ID.
     * 
     * GET /api/{tenantSlug}/products/{id}
     */
    @GetMapping("/{id}")
    public Product getById(@PathVariable String tenantSlug, @PathVariable Long id) {
        Long tenantId = validateTenantAccess(tenantSlug);
        return productService.getProductById(id, tenantId);
    }

    /**
     * Create a new product.
     * 
     * POST /api/{tenantSlug}/products
     */
    @PostMapping
    public Product create(@PathVariable String tenantSlug, @RequestBody Product product) {
        Long tenantId = validateTenantAccess(tenantSlug);
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
        Long tenantId = validateTenantAccess(tenantSlug);
        return productService.updateProduct(id, product, tenantId);
    }

    /**
     * Delete a product (soft delete by setting active=false).
     * 
     * DELETE /api/{tenantSlug}/products/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String tenantSlug, @PathVariable Long id) {
        Long tenantId = validateTenantAccess(tenantSlug);
        productService.deleteProduct(id, tenantId);
    }
}
