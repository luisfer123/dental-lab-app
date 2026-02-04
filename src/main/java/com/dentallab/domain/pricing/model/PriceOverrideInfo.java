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
    
    @Override
    public String toString() {
		return "PriceOverrideInfo{" +
				"adjustment=" + adjustment +
				", reason='" + reason + '\'' +
				", createdAt=" + createdAt +
				", createdBy=" + createdBy +
				'}';
	}
    
    @Override
    public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PriceOverrideInfo that = (PriceOverrideInfo) o;

		if (!adjustment.equals(that.adjustment)) return false;
		if (!reason.equals(that.reason)) return false;
		if (!createdAt.equals(that.createdAt)) return false;
		return createdBy.equals(that.createdBy);
	}
    
    @Override
    public int hashCode() {
		int result = adjustment.hashCode();
		result = 31 * result + reason.hashCode();
		result = 31 * result + createdAt.hashCode();
		result = 31 * result + createdBy.hashCode();
		return result;
    }
}
