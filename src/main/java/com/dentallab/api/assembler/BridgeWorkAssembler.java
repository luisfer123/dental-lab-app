package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.WorkController;
import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;

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
        model.setConstitution(entity.getConstitution());
        model.setBuildingTechnique(entity.getBuildingTechnique());
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
    /**
     * Maps a BridgeWorkModel into a BridgeWorkEntity.
     *
     * @param model the API model
     * @param work  the owning WorkEntity (required for @MapsId)
     * @return populated BridgeWorkEntity
     */
    public static BridgeWorkEntity toEntity(
            BridgeWorkModel model,
            WorkEntity work
    ) {
        if (model == null) {
            return null;
        }

        BridgeWorkEntity entity = new BridgeWorkEntity(work);

        entity.setConstitution(model.getConstitution());
        entity.setBuildingTechnique(model.getBuildingTechnique());
        entity.setCoreMaterialId(model.getCoreMaterialId());
        entity.setVeneeringMaterialId(model.getVeneeringMaterialId());
        entity.setConnectorType(model.getConnectorType());
        entity.setPonticDesign(model.getPonticDesign());
        entity.setNotes(model.getNotes());

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
        if (model.getConstitution() != null)
            entity.setConstitution(model.getConstitution());

        if (model.getBuildingTechnique() != null)
            entity.setBuildingTechnique(model.getBuildingTechnique());

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
