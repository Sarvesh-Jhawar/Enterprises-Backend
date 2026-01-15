package com.tech.enterprise.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "image_name")
    private String imageName;

    private String category;
    private String description;
    private BigDecimal price;
    private String unit;
    private Boolean active = true;

    // Helper method to format the name consistently
    public String generateSimpleImageName() {
        if (this.name == null || this.id == null) return null;
        
        String cleanName = this.name.toLowerCase()
                                 .trim()
                                 .replaceAll("[^a-z0-9]+", "_")
                                 .replaceAll("^_+|_+$", "");
        
        // Format: product_name_tenantID_productID
        return cleanName + "_" + this.tenantId + "_" + this.id;
    }
}