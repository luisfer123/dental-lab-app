package com.dentallab.domain.pricing.query;

import com.dentallab.persistence.entity.WorkFamilyRefEntity;

public interface WorkFamilyLookupQuery {

    WorkFamilyRefEntity findFamilyByWorkId(Long workId);

}
