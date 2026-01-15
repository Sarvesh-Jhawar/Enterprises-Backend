package com.tech.enterprise.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tech.enterprise.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantId(Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
}
