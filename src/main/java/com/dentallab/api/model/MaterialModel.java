package com.dentallab.api.model;

import java.math.BigDecimal;

import org.springframework.hateoas.RepresentationModel;

public class MaterialModel extends RepresentationModel<MaterialModel> {

    private Long id;
    private String name;
    private String category;
    private String unit;
    private BigDecimal pricePerUnit;
    private String status;
    private String notes;

    public MaterialModel() {}

    // Getters + setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}
