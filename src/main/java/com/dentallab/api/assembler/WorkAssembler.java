package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.WorkController;
import com.dentallab.api.model.ClientSummaryModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.service.LookupService;

/**
 * Converts WorkEntity ↔ WorkModel with lookup-table integration.
 * Ensures we expose lookup *codes* in the JSON, not the lookup entities.
 */
@Component
public class WorkAssembler extends RepresentationModelAssemblerSupport<WorkEntity, WorkModel> {

    private final LookupService lookupService;

    public WorkAssembler(LookupService lookupService) {
        super(WorkController.class, WorkModel.class);
        this.lookupService = lookupService;
    }

    // ==========================================================
    // ENTITY → MODEL
    // ==========================================================
    @Override
    public WorkModel toModel(WorkEntity entity) {
        WorkModel model = new WorkModel();

        model.setId(entity.getId());
        model.setDescription(entity.getDescription());
        model.setShade(entity.getShade());
        model.setStatus(entity.getStatus() != null ? entity.getStatus().getCode() : null);
        model.setNotes(entity.getNotes());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        // Lookup codes (safe)
        model.setWorkFamily(entity.getWorkFamily() != null ? entity.getWorkFamily().getCode() : null);
        model.setType(entity.getType() != null ? entity.getType().getCode() : null);

        // Labels
        model.setFamilyLabel(lookupService.getFamilyLabel(entity.getWorkFamily()));
        model.setTypeLabel(lookupService.getTypeLabel(entity.getType()));

        model.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        model.setOrderId(entity.getOrder() != null ? entity.getOrder().getId() : null);

        // HATEOAS links
        model.add(linkTo(methodOn(WorkController.class).getById(entity.getId())).withSelfRel());
        model.add(linkTo(methodOn(WorkController.class).getAll(0, 10, "createdAt,desc")).withRel("collection"));
        
     // ------------------------------------
        // Embedded Client summary
        // ------------------------------------
        if (entity.getClient() != null) {
        	ClientSummaryModel clientModel = new ClientSummaryModel();
        	clientModel.setId(entity.getClient().getId());
        	clientModel.setDisplayName(entity.getClient().getDisplayName());
        	clientModel.setFirstName(entity.getClient().getFirstName());
        	clientModel.setSecondName(entity.getClient().getSecondName());
        	clientModel.setLastName(entity.getClient().getLastName());
        	clientModel.setSecondLastName(entity.getClient().getSecondLastName());
        	clientModel.setPrimaryEmail(entity.getClient().getPrimaryEmail());
        	clientModel.setPrimaryPhone(entity.getClient().getPrimaryPhone());
        	clientModel.setPrimaryAddress(entity.getClient().getPrimaryAddress());
        	clientModel.setActive(entity.getClient().getActive());
        	model.setClient(clientModel);
        }

        return model;
    }

    // ==========================================================
    // MODEL → ENTITY
    // ==========================================================
    public WorkEntity toEntity(WorkModel model) {
        WorkEntity entity = new WorkEntity();
        copyFields(model, entity);
        return entity;
    }

    public void updateEntityFromModel(WorkModel model, WorkEntity entity) {
        copyFields(model, entity);
    }

    // ==========================================================
    // COMMON MAPPER
    // ==========================================================
    private void copyFields(WorkModel model, WorkEntity entity) {

        // --- Lookup conversions ---
        if (model.getWorkFamily() != null) {
            entity.setWorkFamily(lookupService.getFamilyEntity(model.getWorkFamily()));
        }

        if (model.getType() != null) {
            entity.setType(lookupService.getTypeEntity(model.getType()));
        }

        // --- Simple fields ---
        if (model.getDescription() != null) entity.setDescription(model.getDescription());
        if (model.getShade() != null) entity.setShade(model.getShade());
        if (model.getStatus() != null) { entity.setStatus(lookupService.getStatusEntity(model.getStatus())); }
        if (model.getNotes() != null) entity.setNotes(model.getNotes());
    }
}
