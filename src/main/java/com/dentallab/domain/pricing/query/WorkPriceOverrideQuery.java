package com.dentallab.domain.pricing.query;

import java.util.List;

import com.dentallab.persistence.entity.WorkItemPriceOverrideEntity;

public interface WorkPriceOverrideQuery {

    List<WorkItemPriceOverrideEntity> findByWorkPriceId(Long workPriceId);

}
