package com.dentallab.domain.pricing.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import com.dentallab.api.enums.BuildingTechnique;

public final class CreateWorkTypePriceRuleCommand {

    private final String workFamily;
    private final String workType;
    private final String priceGroup;
    private final String constitution;
    private final BuildingTechnique buildingTechnique;
    private final Long coreMaterialId;
    private final String dentureVariant;
    private final String resinType;
    private final String teethType;

    private final BigDecimal basePrice;
    private final BigDecimal pricePerUnit;
    private final String currency;
    private final LocalDate validFrom;
    private final String notes;

    private CreateWorkTypePriceRuleCommand(Builder b) {
        this.workFamily = b.workFamily;
        this.workType = b.workType;
        this.priceGroup = b.priceGroup;
        this.constitution = b.constitution;
        this.buildingTechnique = b.buildingTechnique;
        this.coreMaterialId = b.coreMaterialId;
        this.dentureVariant = b.dentureVariant;
        this.resinType = b.resinType;
        this.teethType = b.teethType;

        this.basePrice = b.basePrice;
        this.pricePerUnit = b.pricePerUnit;
        this.currency = b.currency;
        this.validFrom = b.validFrom;
        this.notes = b.notes;
    }

    // getters only
    public String getWorkFamily() { return workFamily; }
    public String getWorkType() { return workType; }
    public String getPriceGroup() { return priceGroup; }
    public String getConstitution() { return constitution; }
    public BuildingTechnique getBuildingTechnique() { return buildingTechnique; }
    public Long getCoreMaterialId() { return coreMaterialId; }
    public String getDentureVariant() { return dentureVariant; }
    public String getResinType() { return resinType; }
    public String getTeethType() { return teethType; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public String getCurrency() { return currency; }
    public LocalDate getValidFrom() { return validFrom; }
    public String getNotes() { return notes; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String workFamily;
        private String workType;
        private String priceGroup;
        private String constitution;
        private BuildingTechnique buildingTechnique;
        private Long coreMaterialId;
        private String dentureVariant;
        private String resinType;
        private String teethType;

        private BigDecimal basePrice;
        private BigDecimal pricePerUnit;
        private String currency;
        private LocalDate validFrom;
        private String notes;

        private Builder() {}

        public Builder workFamily(String v) { this.workFamily = v; return this; }
        public Builder workType(String v) { this.workType = v; return this; }
        public Builder priceGroup(String v) { this.priceGroup = v; return this; }
        public Builder constitution(String v) { this.constitution = v; return this; }
        public Builder buildingTechnique(BuildingTechnique v) { this.buildingTechnique = v; return this; }
        public Builder coreMaterialId(Long v) { this.coreMaterialId = v; return this; }
        public Builder dentureVariant(String v) { this.dentureVariant = v; return this; }
        public Builder resinType(String v) { this.resinType = v; return this; }
        public Builder teethType(String v) { this.teethType = v; return this; }

        public Builder basePrice(BigDecimal v) { this.basePrice = v; return this; }
        public Builder pricePerUnit(BigDecimal v) { this.pricePerUnit = v; return this; }
        public Builder currency(String v) { this.currency = v; return this; }
        public Builder validFrom(LocalDate v) { this.validFrom = v; return this; }
        public Builder notes(String v) { this.notes = v; return this; }

        public CreateWorkTypePriceRuleCommand build() {
            // defaults
            if (priceGroup == null || priceGroup.isBlank()) priceGroup = "DEFAULT";
            if (currency == null || currency.isBlank()) currency = "MXN";

            // required
            requireText(workFamily, "workFamily is required");
            requireText(workType, "workType is required");
            Objects.requireNonNull(validFrom, "validFrom is required");

            boolean hasBase = basePrice != null;
            boolean hasUnit = pricePerUnit != null;
            if (hasBase == hasUnit) {
                throw new IllegalArgumentException("Exactly one of basePrice or pricePerUnit must be provided");
            }

            if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("basePrice must be >= 0");
            }
            if (pricePerUnit != null && pricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("pricePerUnit must be >= 0");
            }

            return new CreateWorkTypePriceRuleCommand(this);
        }

        private static void requireText(String v, String msg) {
            if (v == null || v.isBlank()) throw new IllegalArgumentException(msg);
        }
    }
}
