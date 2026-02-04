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
    
    @Override
    public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PriceResolution that = (PriceResolution) o;

		if (!basePrice.equals(that.basePrice)) return false;
		if (!totalOverrides.equals(that.totalOverrides)) return false;
		if (!finalPrice.equals(that.finalPrice)) return false;
		if (!currency.equals(that.currency)) return false;
		if (workPriceId != null ? !workPriceId.equals(that.workPriceId) : that.workPriceId != null) return false;
		if (priceGroup != null ? !priceGroup.equals(that.priceGroup) : that.priceGroup != null) return false;
		return overrides.equals(that.overrides);
	}
    
    @Override
    public int hashCode() {
		int result = basePrice.hashCode();
		result = 31 * result + totalOverrides.hashCode();
		result = 31 * result + finalPrice.hashCode();
		result = 31 * result + currency.hashCode();
		result = 31 * result + (workPriceId != null ? workPriceId.hashCode() : 0);
		result = 31 * result + (priceGroup != null ? priceGroup.hashCode() : 0);
		result = 31 * result + overrides.hashCode();
		return result;    					
    }
    
    @Override
    public String toString() {
		return "PriceResolution{" +
				"basePrice=" + basePrice +
				", totalOverrides=" + totalOverrides +
				", finalPrice=" + finalPrice +
				", currency='" + currency + '\'' +
				", workPriceId=" + workPriceId +
				", priceGroup='" + priceGroup + '\'' +
				", overrides=" + overrides +
				'}';
	}
}
