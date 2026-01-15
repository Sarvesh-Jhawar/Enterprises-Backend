package com.tech.enterprise.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tech.enterprise.model.Product;
import com.tech.enterprise.repo.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    
    @Transactional //(readOnly = true)
    public List<Product> getProductsByTenantId(Long tenantId) {
        return productRepository.findByTenantId(tenantId);
    }

    @Transactional
    public Product saveProduct(Product product, Long tenantId){
        // 1. Validation for unique name within the same tenant
        if(productRepository.existsByNameAndTenantId(product.getName(), tenantId)) {
            throw new IllegalArgumentException("Product with the same name already exists for this tenant.");
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

    @Transactional
    public Product updateProduct(Long id, Product details, Long tenantId) {
        Product existing = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

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
     * Helper method to generate the simple name format
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