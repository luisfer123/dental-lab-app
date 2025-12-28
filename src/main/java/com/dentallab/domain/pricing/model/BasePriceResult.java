package com.dentallab.domain.pricing.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Result of resolving a base price from pricing rules (work_type_price)
 * or from a fixed per-work snapshot (work_price).
 */
public class BasePriceResult {

    private final BigDecimal basePrice;
    private final String currency;
    private final String priceGroup;

    // Optional: from work_type_price rule (null if base came from fixed work_price)
    private final Long workTypePriceId;

    public BasePriceResult(BigDecimal basePrice,
                           String currency,
                           String priceGroup,
                           Long workTypePriceId) {
        this.basePrice = Objects.requireNonNull(basePrice);
        this.currency = Objects.requireNonNull(currency);
        this.priceGroup = Objects.requireNonNull(priceGroup);
        this.workTypePriceId = workTypePriceId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPriceGroup() {
        return priceGroup;
    }

    public Long getWorkTypePriceId() {
        return workTypePriceId;
    }
}
