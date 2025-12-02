package com.dentallab.api.assembler;

import com.dentallab.api.controller.WorkController;
import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.BridgeWorkEntity;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * Aggregates WorkEntity with its extension tables (crown/bridge)
 * into a unified FullWorkModel for API responses.
 */
@Component
public class FullWorkAssembler extends RepresentationModelAssemblerSupport<WorkEntity, FullWorkModel> {

    private final WorkAssembler workAssembler;
    private final CrownWorkAssembler crownWorkAssembler;
    private final BridgeWorkAssembler bridgeWorkAssembler;

    public FullWorkAssembler(
            WorkAssembler workAssembler,
            CrownWorkAssembler crownWorkAssembler,
            BridgeWorkAssembler bridgeWorkAssembler) {

        super(WorkController.class, FullWorkModel.class);
        this.workAssembler = workAssembler;
        this.crownWorkAssembler = crownWorkAssembler;
        this.bridgeWorkAssembler = bridgeWorkAssembler;
    }

    @Override
    public FullWorkModel toModel(WorkEntity entity) {
        if (entity == null) return null;

        WorkModel base = workAssembler.toModel(entity);

        FullWorkModel model = new FullWorkModel();
        model.setBase(base);

        model.setWorkFamily(entity.getWorkFamily() != null ? entity.getWorkFamily().getCode() : null);
        model.setType(entity.getType() != null ? entity.getType().getCode() : null);

        model.setFamilyLabel(base.getFamilyLabel());
        model.setTypeLabel(base.getTypeLabel());
        
        model.setInternalCode(base.getInternalCode());

        String type = model.getType();

        if (type != null) {
            switch (type.toUpperCase()) {
                case "CROWN" -> {
                    CrownWorkEntity crown = entity.getCrownWork();
                    if (crown != null) model.setExtension(crownWorkAssembler.toModel(crown));
                }
                case "BRIDGE" -> {
                    BridgeWorkEntity bridge = entity.getBridgeWork();
                    if (bridge != null) model.setExtension(bridgeWorkAssembler.toModel(bridge));
                }
            }
        }

        model.add(base.getLinks());
        return model;
    }

    public WorkEntity toEntity(FullWorkModel model) {
        if (model == null || model.getBase() == null) return null;
        return workAssembler.toEntity(model.getBase());
    }

    public void updateEntityFromModel(FullWorkModel model, WorkEntity entity) {
        if (model != null && model.getBase() != null) {
            workAssembler.updateEntityFromModel(model.getBase(), entity);
        }
    }
}
