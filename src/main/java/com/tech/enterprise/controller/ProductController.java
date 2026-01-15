package com.tech.enterprise.controller;


import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.enterprise.model.Product;
import com.tech.enterprise.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows your frontend to talk to the backend
public class ProductController {

    private final ProductService productService;

    // 1. Get all products for your enterprise
    @GetMapping
    public List<Product> getAll(@RequestHeader("X-Tenant-ID") Long tenantId) {
        return productService.getProductsByTenantId(tenantId);
    }

    // 2. Create a new product
    @PostMapping
    public Product create(@RequestBody Product product, @RequestHeader("X-Tenant-ID") Long tenantId) {
        return productService.saveProduct(product, tenantId);
    }

    // 3. Update an existing product
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, 
                          @RequestBody Product product, 
                          @RequestHeader("X-Tenant-ID") Long tenantId) {
        return productService.updateProduct(id, product, tenantId);
    }
}
