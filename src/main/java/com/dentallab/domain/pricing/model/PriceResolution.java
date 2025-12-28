package com.dentallab.domain.pricing.model;

import java.math.BigDecimal;
import java.util.List;

public class PriceResolution {

    private final BigDecimal basePrice;
    private final BigDecimal totalOverrides;
    private final BigDecimal finalPrice;
    private final String currency;

    private final Long workPriceId;
    private final String priceGroup;

    private final List<PriceOverrideInfo> overrides;

    public PriceResolution(BigDecimal basePrice,
                           BigDecimal totalOverrides,
                           BigDecimal finalPrice,
                           String currency,
                           Long workPriceId,
                           String priceGroup,
                           List<PriceOverrideInfo> overrides) {
        this.basePrice = basePrice;
        this.totalOverrides = totalOverrides;
        this.finalPrice = finalPrice;
        this.currency = currency;
        this.workPriceId = workPriceId;
        this.priceGroup = priceGroup;
        this.overrides = List.copyOf(overrides);
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getTotalOverrides() {
        return totalOverrides;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getWorkPriceId() {
        return workPriceId;
    }

    public String getPriceGroup() {
        return priceGroup;
    }

    public List<PriceOverrideInfo> getOverrides() {
        return overrides;
    }
}
