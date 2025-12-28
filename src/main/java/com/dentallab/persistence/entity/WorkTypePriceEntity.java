package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "work_type_price")
public class WorkTypePriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "work_family", nullable = false)
    private String workFamily;

    @Column(name = "work_type", nullable = false)
    private String workType;

    @Column(name = "price_group", nullable = false)
    private String priceGroup;

    /**
     * Monolithic or stratified. (May more values in future.
     */
    @Column(name = "constitution")
    private String constitution;

    /**
     * Could be Monolithic, stratified, etc.
     */
    @Column(name = "building_technique")
    private String buildingTechnique;

    /**
     * When monolithic it is just material. When stratified it is core material.
     */
    @Column(name = "core_material_id")
    private Long coreMaterialId;

    /**
     * When no null it is assume as the price. When null pricePerUnit is used
     */
    @Column(name = "base_price")
    private BigDecimal basePrice;
    
    /** 
     * Used when work is priced by its units. 
     * (for instance in a bridge each tooth is an unit and price is pricePerUnit*#ofUnits)
     * When {@linkplain basePrice} is present and not null, it is used as price without even reading pricePerUnit. 
     */
    @Column(name = "price_per_unit")
    private BigDecimal pricePerUnit;

    @Column(name = "currency")
    private String currency;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    // ---------------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------------

    public Long getPriceId() {
        return priceId;
    }

    public String getWorkFamily() {
        return workFamily;
    }

    public String getWorkType() {
        return workType;
    }

    public String getPriceGroup() {
        return priceGroup;
    }

    public String getConstitution() {
        return constitution;
    }

    public String getBuildingTechnique() {
        return buildingTechnique;
    }

    public Long getCoreMaterialId() {
        return coreMaterialId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    // ---------------------------------------------------------------------
    // Setters
    // ---------------------------------------------------------------------

    public void setPriceId(Long priceId) {
        this.priceId = priceId;
    }

    public void setWorkFamily(String workFamily) {
        this.workFamily = workFamily;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public void setPriceGroup(String priceGroup) {
        this.priceGroup = priceGroup;
    }

    public void setConstitution(String constitution) {
        this.constitution = constitution;
    }

    public void setBuildingTechnique(String buildingTechnique) {
        this.buildingTechnique = buildingTechnique;
    }

    public void setCoreMaterialId(Long coreMaterialId) {
        this.coreMaterialId = coreMaterialId;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    // ---------------------------------------------------------------------
    // equals / hashCode (JPA identity-based)
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkTypePriceEntity that)) return false;
        return priceId != null && priceId.equals(that.priceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(priceId);
    }

    // ---------------------------------------------------------------------
    // toString (safe for logs)
    // ---------------------------------------------------------------------

    @Override
    public String toString() {
        return "WorkTypePriceEntity{" +
                "priceId=" + priceId +
                ", workFamily='" + workFamily + '\'' +
                ", workType='" + workType + '\'' +
                ", priceGroup='" + priceGroup + '\'' +
                ", constitution='" + constitution + '\'' +
                ", buildingTechnique='" + buildingTechnique + '\'' +
                ", coreMaterialId=" + coreMaterialId +
                ", basePrice=" + basePrice +
                ", pricePerUnit=" + pricePerUnit +
                ", currency='" + currency + '\'' +
                ", validFrom=" + validFrom +
                '}';
    }
}

