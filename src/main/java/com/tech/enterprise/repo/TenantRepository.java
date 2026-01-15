package com.tech.enterprise.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tech.enterprise.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlugAndActiveTrue(String slug);

    Optional<Tenant> findBySlug(String slug);
}
