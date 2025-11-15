package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a generic material definition (e.g., Noritake EX-3 Body A2B, e.max Press LT A2).
 */
@Entity
@Table(name = "material")
public class MaterialEntity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MaterialItemEntity> materialItems = new ArrayList<>();

    // ===== Constructors =====
    public MaterialEntity() {}
    public MaterialEntity(String name, String category, String unit, BigDecimal pricePerUnit) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
    }

    // ===== Getters and Setters (grouped per field) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<MaterialItemEntity> getMaterialItems() { return materialItems; }
    public void setMaterialItems(List<MaterialItemEntity> materialItems) { this.materialItems = materialItems; }

    // ===== Helper methods =====
    public void addMaterialItem(MaterialItemEntity item) {
        materialItems.add(item);
        item.setMaterial(this);
    }

    public void removeMaterialItem(MaterialItemEntity item) {
        materialItems.remove(item);
        item.setMaterial(null);
    }

    // ===== equals / hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialEntity)) return false;
        MaterialEntity that = (MaterialEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // ===== toString =====
    @Override
    public String toString() {
        return "MaterialEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", unit='" + unit + '\'' +
                ", pricePerUnit=" + pricePerUnit +
                ", status='" + status + '\'' +
                '}';
    }
}

/**

			ER diagram:
			
			material        (1) ───< (∞) material_item
			|                          |
			|                          |
			|                        (∞)
			|                          |
			└──────< (∞) material_usage >────── (∞) work

**/