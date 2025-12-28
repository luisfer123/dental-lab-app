package com.dentallab.service.impl;

import org.springframework.stereotype.Service;

import com.dentallab.api.model.CrownWorkModel;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.service.CrownWorkService;

@Service
public class CrownWorkServiceImpl implements CrownWorkService {
	
    public CrownWorkServiceImpl() {
    }
    
    @Override
    public CrownWorkEntity toEntity(CrownWorkModel model, WorkEntity work) {

        CrownWorkEntity entity = new CrownWorkEntity();
        entity.setWork(work);

        entity.setNotes(model.getNotes());
        entity.setToothNumber(model.getToothNumber());
        entity.setConstitution(model.getConstitution());
        entity.setBuildingTechnique(model.getBuildingTechnique());
        entity.setCoreMaterialId(model.getCoreMaterialId());
        entity.setVeneeringMaterialId(model.getVeneeringMaterialId());
        entity.setIsMonolithic(model.getIsMonolithic());

        return entity;
    }

}
