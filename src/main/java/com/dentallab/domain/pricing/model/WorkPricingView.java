package com.dentallab.domain.pricing.model;

public interface WorkPricingView {

	Long getWorkId();

    String getWorkFamily();
    String getWorkType();

    // Pricing attributes (used conditionally by family rules)
    String getConstitution();
    String getBuildingTechnique();
    Long getCoreMaterialId();

    Integer getProstheticUnits(); // Null for unitary works
}
