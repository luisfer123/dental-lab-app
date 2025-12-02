package com.dentallab.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.service.BridgeWorkService;

@Service
public class BridgeWorkServiceImpl implements BridgeWorkService {
	
	private static final Logger log = LoggerFactory.getLogger(BridgeWorkService.class);


    public BridgeWorkServiceImpl() {
    }

    /**
     * Maps a BridgeWorkModel → BridgeWorkEntity and attaches it to the given WorkEntity.
     * All required lookups (work type ref) and validations are handled here.
     */
    public BridgeWorkEntity toEntity(BridgeWorkModel model, WorkEntity work) {

        Objects.requireNonNull(model, "BridgeWorkModel cannot be null");
        Objects.requireNonNull(work, "WorkEntity cannot be null");
        Objects.requireNonNull(model.getType(), "Bridge extension must include type");

        log.info("Mapping BridgeWorkModel to entity. workId={}, type={}",
                work.getId(), model.getType());

        BridgeWorkEntity entity = new BridgeWorkEntity();

        // Mandatory relationship
        entity.setWork(work);

        // Common fields
        entity.setNotes(model.getNotes());

        // Bridge-specific
        entity.setVariant(model.getBridgeVariant());
        entity.setBuildingTechnique(model.getBuildingTechnique());

        entity.setAbutmentTeeth(model.getAbutmentTeeth());
        entity.setPonticTeeth(model.getPonticTeeth());

        entity.setCoreMaterialId(model.getCoreMaterialId());
        entity.setVeneeringMaterialId(model.getVeneeringMaterialId());

        entity.setConnectorType(model.getConnectorType());
        entity.setPonticDesign(model.getPonticDesign());

        return entity;
    }

}
