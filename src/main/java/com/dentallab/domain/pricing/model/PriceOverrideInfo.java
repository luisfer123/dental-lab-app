package com.dentallab.domain.pricing.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PriceOverrideInfo {

    private final BigDecimal adjustment;
    private final String reason;
    private final Instant createdAt;
    private final Long createdBy;

    public PriceOverrideInfo(BigDecimal adjustment,
                             String reason,
                             Instant createdAt,
                             Long createdBy) {
        this.adjustment = adjustment;
        this.reason = reason;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
