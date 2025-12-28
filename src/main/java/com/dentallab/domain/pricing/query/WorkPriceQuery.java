package com.dentallab.domain.pricing.query;

import java.util.Optional;

import com.dentallab.persistence.entity.WorkPriceEntity;

public interface WorkPriceQuery {

    Optional<WorkPriceEntity> findByWorkId(Long workId);

}
