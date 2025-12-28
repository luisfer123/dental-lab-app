package com.dentallab.domain.pricing.service;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;

public interface WorkBasePriceService {

    BasePriceResult previewBasePrice(PriceResolutionRequest request);

    void fixBasePrice(Long workId, BasePriceResult basePrice);
}
