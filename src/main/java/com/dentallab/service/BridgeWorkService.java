package com.dentallab.service;

import com.dentallab.api.dto.BridgeWorkUpsertRequest;
import com.dentallab.persistence.entity.BridgeWorkEntity;

public interface BridgeWorkService {
	
	/**
     * Creates or updates the bridge-specific data for an existing work.
     *
     * Contract:
     * - The work with the given workId must already exist.
     * - Bridge teeth are fully replaced by the provided list.
     * - Validation errors result in IllegalArgumentException.
     *
     * @param workId the id of the parent work
     * @param request bridge data including normalized teeth
     * @return the persisted BridgeWorkEntity
     */
    BridgeWorkEntity upsertBridgeWork(Long workId, BridgeWorkUpsertRequest request);

}
