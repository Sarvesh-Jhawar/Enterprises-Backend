package com.tech.enterprise.service;

import java.util.List;
import com.tech.enterprise.model.ProductVariant;
import com.tech.enterprise.repo.ProductVariantRepository;

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
    private final ProductVariantRepository variantRepository;

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
     * Get variants for a product, ensuring tenant isolation.
     */
    @Transactional
    public List<ProductVariant> getProductVariants(Long productId, Long tenantId) {
        // Ensure the product exists and belongs to the tenant
        getProductById(productId, tenantId);
        return variantRepository.findByProductIdAndTenantId(productId, tenantId);
    }

    /**
     * Create a new product for the specified tenant.
     */
    @Transactional
    public Product saveProduct(Product product, Long tenantId) {
        // 1. Validation
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required.");
        }

        if (productRepository.existsByNameAndTenantId(product.getName(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product with the same name already exists for this tenant.");
        }

        // 2. Initial save to generate the Database ID
        product.setTenantId(tenantId);
        Product savedProduct = productRepository.save(product);

        // 3. Generate the simple image name: product_name_tenantID_productID
        String simpleImageName = generateSimpleName(savedProduct);
        savedProduct.setImageName(simpleImageName);

        // 4. Final save to store the image name
        return productRepository.save(savedProduct);
    }

    /**
     * Update an existing product, ensuring tenant isolation.
     */
    @Transactional
    public Product updateProduct(Long id, Product details, Long tenantId) {
        Product existing = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (details.getName() == null || details.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required.");
        }

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
        existing.setImageName(generateSimpleName(existing));

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

    /**
     * Helper method to generate the simple name format.
     */
    private String generateSimpleName(Product product) {
        String cleanName = product.getName().toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        // Format: name_tenantId_productId
        return cleanName + "_" + product.getTenantId() + "_" + product.getId();
    }
}