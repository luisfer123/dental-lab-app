package com.dentallab.domain.pricing.query;

import java.time.LocalDate;
import java.util.Optional;

import com.dentallab.persistence.entity.WorkTypePriceEntity;

public interface WorkTypePriceQuery {

    Optional<WorkTypePriceEntity> findBestMatch(
            String workFamily,
            String workType,
            String priceGroup,
            String constitution,
            String buildingTechnique,
            Long coreMaterialId,
            LocalDate pricingDate
    );
}
