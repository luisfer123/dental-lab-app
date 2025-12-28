package com.dentallab.api.assembler;

import com.dentallab.api.controller.WorkController;
import com.dentallab.api.model.CrownWorkModel;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Converts CrownWorkEntity ↔ CrownWorkModel and adds HATEOAS links.
 * Mirrors the style of WorkAssembler for consistency.
 */
@Component
public class CrownWorkAssembler extends RepresentationModelAssemblerSupport<CrownWorkEntity, CrownWorkModel> {

    public CrownWorkAssembler() {
        super(WorkController.class, CrownWorkModel.class);
    }

    /* ==========================================================
       ENTITY → MODEL (for API responses)
       ========================================================== */
    @Override
    public CrownWorkModel toModel(CrownWorkEntity entity) {
        CrownWorkModel model = instantiateModel(entity);

        // Base work linkage
        model.setWorkId(entity.getWork() != null ? entity.getWork().getId() : entity.getId());

        // Common fields
        model.setNotes(entity.getNotes());

        // Crown-specific fields
        model.setToothNumber(entity.getToothNumber());
        model.setConstitution(entity.getConstitution());
        model.setBuildingTechnique(entity.getBuildingTechnique());
        model.setCoreMaterialId(entity.getCoreMaterialId());
        model.setVeneeringMaterialId(entity.getVeneeringMaterialId());
        model.setIsMonolithic(entity.getIsMonolithic());

        // ----------------------------
        // HATEOAS links
        // ----------------------------
        if (model.getWorkId() != null) {
            model.add(linkTo(methodOn(WorkController.class).getById(model.getWorkId()))
                    .withRel("work"));
        }

        // Default collection link (safe defaults)
        model.add(linkTo(methodOn(WorkController.class).getAll(0, 10, "createdAt,desc"))
                .withRel("collection"));

        return model;
    }

    /* ==========================================================
       MODEL → ENTITY (for creates/updates)
       ========================================================== */
    public CrownWorkEntity toEntity(CrownWorkModel model, WorkEntity work) {

        CrownWorkEntity entity = new CrownWorkEntity();

        // Assign FK
        entity.setWork(work);

        // Common fields
        entity.setNotes(model.getNotes());

        // Crown-specific
        entity.setToothNumber(model.getToothNumber());
        entity.setConstitution(model.getConstitution());
        entity.setBuildingTechnique(model.getBuildingTechnique());
        entity.setCoreMaterialId(model.getCoreMaterialId());
        entity.setVeneeringMaterialId(model.getVeneeringMaterialId());
        entity.setIsMonolithic(model.getIsMonolithic());

        return entity;
    }


    /* ==========================================================
       Partial update helper (PATCH / PUT)
       ========================================================== */
    public void updateEntityFromModel(CrownWorkModel model, CrownWorkEntity entity) {
        copyCommonFields(model, entity);
    }

    /* ==========================================================
       Shared mapper for reusable field copying
       ========================================================== */
    private void copyCommonFields(CrownWorkModel model, CrownWorkEntity entity) {

        // Base field
        if (model.getNotes() != null)
            entity.setNotes(model.getNotes());

        // Crown-specific
        if (model.getToothNumber() != null)
            entity.setToothNumber(model.getToothNumber());

        if (model.getConstitution() != null)
            entity.setConstitution(model.getConstitution());

        if (model.getBuildingTechnique() != null)
            entity.setBuildingTechnique(model.getBuildingTechnique());

        if (model.getCoreMaterialId() != null)
            entity.setCoreMaterialId(model.getCoreMaterialId());

        if (model.getVeneeringMaterialId() != null)
            entity.setVeneeringMaterialId(model.getVeneeringMaterialId());

        if (model.getIsMonolithic() != null)
            entity.setIsMonolithic(model.getIsMonolithic());
    }
}
