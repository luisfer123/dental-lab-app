package com.dentallab.api.assembler;

import com.dentallab.api.controller.WorkController;
import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Converts BridgeWorkEntity ↔ BridgeWorkModel and adds HATEOAS links.
 * Provides bidirectional mapping for bridge-type dental work.
 */
@Component
public class BridgeWorkAssembler extends RepresentationModelAssemblerSupport<BridgeWorkEntity, BridgeWorkModel> {

    public BridgeWorkAssembler() {
        super(WorkController.class, BridgeWorkModel.class);
    }

    /* ==========================================================
       ENTITY → MODEL (for API responses)
       ========================================================== */
    @Override
    public BridgeWorkModel toModel(BridgeWorkEntity entity) {
        BridgeWorkModel model = instantiateModel(entity);

        // Link back to Work (shared primary key)
        model.setWorkId(entity.getWork() != null ? entity.getWork().getId() : entity.getId());

        // Map all fields
        model.setBridgeVariant(entity.getVariant());
        model.setBuildingTechnique(entity.getBuildingTechnique());
        model.setAbutmentTeeth(entity.getAbutmentTeeth());
        model.setPonticTeeth(entity.getPonticTeeth());
        model.setCoreMaterialId(entity.getCoreMaterialId());
        model.setVeneeringMaterialId(entity.getVeneeringMaterialId());
        model.setConnectorType(entity.getConnectorType());
        model.setPonticDesign(entity.getPonticDesign());
        model.setNotes(entity.getNotes());

        // ----------------------------
        // HATEOAS links
        // ----------------------------
        if (model.getWorkId() != null) {
            model.add(linkTo(methodOn(WorkController.class).getById(model.getWorkId()))
                    .withRel("work"));
        }

        // Safe default link to collection
        model.add(linkTo(methodOn(WorkController.class).getAll(0, 10, "createdAt,desc"))
                .withRel("collection"));

        return model;
    }

    /* ==========================================================
       MODEL → ENTITY (for creates/updates)
       ========================================================== */
    public BridgeWorkEntity toEntity(BridgeWorkModel model) {
        BridgeWorkEntity entity = new BridgeWorkEntity();
        copyCommonFields(model, entity);
        return entity;
    }

    /* ==========================================================
       Partial update helper (PATCH / PUT)
       ========================================================== */
    public void updateEntityFromModel(BridgeWorkModel model, BridgeWorkEntity entity) {
        copyCommonFields(model, entity);
    }

    /* ==========================================================
       Shared mapper for reusable field copying
       ========================================================== */
    private void copyCommonFields(BridgeWorkModel model, BridgeWorkEntity entity) {

        // Base field
        if (model.getNotes() != null)
            entity.setNotes(model.getNotes());

        // Bridge-specific fields
        if (model.getBridgeVariant() != null)
            entity.setVariant(model.getBridgeVariant());

        if (model.getBuildingTechnique() != null)
            entity.setBuildingTechnique(model.getBuildingTechnique());

        if (model.getAbutmentTeeth() != null)
            entity.setAbutmentTeeth(model.getAbutmentTeeth());

        if (model.getPonticTeeth() != null)
            entity.setPonticTeeth(model.getPonticTeeth());

        if (model.getCoreMaterialId() != null)
            entity.setCoreMaterialId(model.getCoreMaterialId());

        if (model.getVeneeringMaterialId() != null)
            entity.setVeneeringMaterialId(model.getVeneeringMaterialId());

        if (model.getConnectorType() != null)
            entity.setConnectorType(model.getConnectorType());

        if (model.getPonticDesign() != null)
            entity.setPonticDesign(model.getPonticDesign());
    }
}
