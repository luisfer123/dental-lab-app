package com.dentallab.domain.pricing.query;

import com.dentallab.domain.pricing.model.WorkPricingView;

public interface WorkPricingQuery {

    WorkPricingView findByWorkId(Long workId);

}
