package com.dentallab.domain.pricing.service;

import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;

public interface WorkPricingService {
	
	PriceResolution resolveFinalPrice(PriceResolutionRequest request);

}
