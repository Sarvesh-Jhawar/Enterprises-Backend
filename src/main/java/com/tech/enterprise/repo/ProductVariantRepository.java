package com.tech.enterprise.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tech.enterprise.model.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndTenantId(Long productId, Long tenantId);

    Optional<ProductVariant> findByIdAndTenantId(Long id, Long tenantId);
}
