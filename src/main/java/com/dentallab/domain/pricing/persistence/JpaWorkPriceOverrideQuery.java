package com.dentallab.domain.pricing.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dentallab.domain.pricing.query.WorkPriceOverrideQuery;
import com.dentallab.persistence.entity.WorkItemPriceOverrideEntity;

@Repository
public interface JpaWorkPriceOverrideQuery
        extends org.springframework.data.repository.Repository<WorkItemPriceOverrideEntity, Long>,
                WorkPriceOverrideQuery {

    @Override
    List<WorkItemPriceOverrideEntity> findByWorkPriceId(Long workPriceId);
}
