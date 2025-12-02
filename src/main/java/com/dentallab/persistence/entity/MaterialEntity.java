package com.dentallab.persistence.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "material")
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String category;

    private String unit;

    @Column(name = "price_per_unit", nullable = false)
    private BigDecimal pricePerUnit;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public MaterialEntity() {}

    public MaterialEntity(String name, String category, String unit, double price) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.pricePerUnit = BigDecimal.valueOf(price);
        this.status = "ACTIVE";
    }

    // Getters + setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }

    public String getUnit() { return unit; }
    public void setUnit(String u) { this.unit = u; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal p) { this.pricePerUnit = p; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }

    @Override
    public String toString() {
        return "MaterialEntity{id=%d, name='%s'}"
                .formatted(id, name);
    }
}
