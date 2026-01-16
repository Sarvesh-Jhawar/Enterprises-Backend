package com.tech.enterprise.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.model.Product;
import com.tech.enterprise.repo.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service for product CRUD operations with tenant isolation.
 * All operations are scoped to the specified tenant.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products for a tenant.
     */
    @Transactional
    public List<Product> getProductsByTenantId(Long tenantId) {
        return productRepository.findByTenantId(tenantId);
    }

    /**
     * Get a single product by ID, ensuring tenant isolation.
     */
    @Transactional
    public Product getProductById(Long id, Long tenantId) {
        return productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));
    }

    /**
     * Create a new product for the specified tenant.
     */
    @Transactional
    public Product saveProduct(Product product, Long tenantId) {
        // 1. Validation for unique name within the same tenant
        if (productRepository.existsByNameAndTenantId(product.getName(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product with the same name already exists for this tenant.");
        }

        // 2. Set the tenant context
        product.setTenantId(tenantId);

        // 3. Generate a unique image name BEFORE the first save
        // This satisfies the NOT NULL constraint in your database
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String cleanName = product.getName().toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        // Format: name_tenantId_uniqueSuffix
        product.setImageName(cleanName + "_" + tenantId + "_" + uniqueSuffix);

        // 4. Save the product once
        return productRepository.save(product);
    }

    /**
     * Update an existing product, ensuring tenant isolation.
     */
    @Transactional
    public Product updateProduct(Long id, Product details, Long tenantId) {
        Product existing = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        // Check if name is being changed to an existing name
        if (!existing.getName().equals(details.getName()) &&
                productRepository.existsByNameAndTenantId(details.getName(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product with the same name already exists for this tenant.");
        }

        // Update fields
        existing.setName(details.getName());
        existing.setCategory(details.getCategory());
        existing.setDescription(details.getDescription());
        existing.setPrice(details.getPrice());
        existing.setUnit(details.getUnit());
        existing.setActive(details.getActive());

        // Regenerate image name in case the product name was changed

        return productRepository.save(existing);
    }

    /**
     * Soft delete a product by setting active=false.
     */
    @Transactional
    public void deleteProduct(Long id, Long tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }

}