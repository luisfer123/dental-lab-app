package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a physical batch or container of a material
 * (e.g., Batch EX3-A2B-0925, barcode 1234567890, 50 g in store).
 */
@Entity
@Table(name = "material_item")
public class MaterialItemEntity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialEntity material;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "barcode", length = 100, unique = true)
    private String barcode;

    @Column(name = "status", length = 50)
    private String status = "IN_STORE";

    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit = "g";

    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(name = "date_used")
    private LocalDate dateUsed;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    // ===== Constructors =====
    public MaterialItemEntity() {}
    public MaterialItemEntity(MaterialEntity material, String batchNumber, BigDecimal quantity) {
        this.material = material;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
    }

    // ===== Getters and Setters (grouped per field) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MaterialEntity getMaterial() { return material; }
    public void setMaterial(MaterialEntity material) { this.material = material; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }

    public LocalDate getDateUsed() { return dateUsed; }
    public void setDateUsed(LocalDate dateUsed) { this.dateUsed = dateUsed; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    // ===== equals / hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialItemEntity)) return false;
        MaterialItemEntity that = (MaterialItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // ===== toString =====
    @Override
    public String toString() {
        return "MaterialItemEntity{" +
                "id=" + id +
                ", material=" + (material != null ? material.getId() : null) +
                ", batchNumber='" + batchNumber + '\'' +
                ", barcode='" + barcode + '\'' +
                ", status='" + status + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", dateReceived=" + dateReceived +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
