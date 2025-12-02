package com.dentallab.service;

import com.dentallab.api.model.CrownWorkModel;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;

public interface CrownWorkService {
	
	CrownWorkEntity toEntity(CrownWorkModel model, WorkEntity baseWork);

}
