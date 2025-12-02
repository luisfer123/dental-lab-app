package com.dentallab.service;

import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;

public interface BridgeWorkService {
	
	BridgeWorkEntity toEntity(BridgeWorkModel model, WorkEntity work);

}
