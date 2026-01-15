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
@Table(name = "product_variants")
@Data
@NoArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "quantity_value", nullable = false)
    private BigDecimal quantityValue;

    @Column(name = "quantity_unit", nullable = false)
    private String quantityUnit;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active = true;
}
