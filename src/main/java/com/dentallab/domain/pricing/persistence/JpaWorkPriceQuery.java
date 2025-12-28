package com.dentallab.domain.pricing.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.dentallab.domain.pricing.query.WorkPriceQuery;
import com.dentallab.persistence.entity.WorkPriceEntity;

@Repository
public interface JpaWorkPriceQuery
        extends org.springframework.data.repository.Repository<WorkPriceEntity, Long>,
                WorkPriceQuery {

    @Override
    Optional<WorkPriceEntity> findByWorkId(Long workId);
}
