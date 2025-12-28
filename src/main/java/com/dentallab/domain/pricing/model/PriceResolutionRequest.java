package com.dentallab.domain.pricing.model;

import java.time.LocalDate;
import java.util.Objects;

public class PriceResolutionRequest {

    private final Long workId;
    private final LocalDate pricingDate;
    private final String priceGroup;

    public PriceResolutionRequest(Long workId,
                                  LocalDate pricingDate,
                                  String priceGroup) {
        this.workId = Objects.requireNonNull(workId);
        this.pricingDate = pricingDate;
        this.priceGroup = priceGroup;
    }
    
    /**
     * Returns a copy of this request bound to the given workId.
     * Intended to be used at API boundaries where the work identity
     * is provided by the path variable.
     * 
     * @param workId the work identifier
     * @return a new PriceResolutionRequest with the field in the 
     * current instance and the give workId
     */
    public PriceResolutionRequest withWorkId(Long workId) {
		return new PriceResolutionRequest(
				Objects.requireNonNull(workId),
				this.pricingDate,
				this.priceGroup);
	}
    
    /**
	 * Creates a PriceResolutionRequest for final price resolution.
	 * 
	 * In final price resolution, only the workId is required.
	 * It is basically a wrapper for workId suitable for final price resolution.
	 * 
	 * @param workId the work identifier
	 * @return a PriceResolutionRequest with only workId not null. 
	 */
    public static PriceResolutionRequest forWork(Long workId) {
        return new PriceResolutionRequest(
                Objects.requireNonNull(workId),
                null,        // pricingDate not required for final resolution
                null         // priceGroup not required for final resolution
        );
    }

    public Long getWorkId() {
        return workId;
    }

    public LocalDate getPricingDate() {
        return pricingDate;
    }

    public String getPriceGroup() {
        return priceGroup;
    }
}
