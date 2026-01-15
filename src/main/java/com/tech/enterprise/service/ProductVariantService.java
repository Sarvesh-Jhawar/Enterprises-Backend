package com.tech.enterprise.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tech.enterprise.model.ProductVariant;
import com.tech.enterprise.repo.ProductVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductService productService;

    @Transactional
    public List<ProductVariant> getVariantsByProductId(Long productId, Long tenantId) {
        // Ensure the product exists and belongs to the tenant
        productService.getProductById(productId, tenantId);
        return variantRepository.findByProductIdAndTenantId(productId, tenantId);
    }

    @Transactional
    public ProductVariant saveVariant(Long productId, ProductVariant variant, Long tenantId) {
        // Ensure product exists and belongs to tenant
        productService.getProductById(productId, tenantId);

        variant.setProductId(productId);
        variant.setTenantId(tenantId);
        return variantRepository.save(variant);
    }

    @Transactional
    public ProductVariant updateVariant(Long variantId, ProductVariant details, Long tenantId) {
        ProductVariant existing = variantRepository.findByIdAndTenantId(variantId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Variant not found"));

        existing.setQuantityValue(details.getQuantityValue());
        existing.setQuantityUnit(details.getQuantityUnit());
        existing.setPrice(details.getPrice());
        existing.setActive(details.getActive());

        return variantRepository.save(existing);
    }

    @Transactional
    public void deleteVariant(Long variantId, Long tenantId) {
        ProductVariant existing = variantRepository.findByIdAndTenantId(variantId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Variant not found"));

        existing.setActive(false);
        variantRepository.save(existing);
    }
}
