package com.tech.enterprise.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tech.enterprise.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsernameAndTenantIdAndActiveTrue(String username, Long tenantId);

    Optional<Admin> findByUsernameAndTenantId(String username, Long tenantId);
}
