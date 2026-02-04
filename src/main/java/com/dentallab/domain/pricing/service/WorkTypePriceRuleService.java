package com.dentallab.domain.pricing.service;

import com.dentallab.domain.pricing.model.CreateWorkTypePriceRuleCommand;
import com.dentallab.persistence.entity.WorkTypePriceEntity;

public interface WorkTypePriceRuleService {
    WorkTypePriceEntity createRule(CreateWorkTypePriceRuleCommand command);
}
