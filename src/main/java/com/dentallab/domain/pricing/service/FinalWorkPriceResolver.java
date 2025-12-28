package com.dentallab.domain.pricing.service;

import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;

/**
 * Resolves the final price of a work using a previously fixed base price
 * and any applied overrides.
 *
 * Implementations assume that a base price already exists for the work.
 */
public interface FinalWorkPriceResolver {

    PriceResolution resolve(PriceResolutionRequest request);

}
