package com.dentallab.api.assembler;

import com.dentallab.api.controller.ClientController;
import com.dentallab.api.model.ClientModel;
import com.dentallab.persistence.entity.ClientEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Converts ClientEntity ↔ ClientModel and adds HATEOAS links.
 * Uses UTC conversion for timestamps.
 */
@Component
public class ClientAssembler extends RepresentationModelAssemblerSupport<ClientEntity, ClientModel> {

    public ClientAssembler() {
        super(ClientController.class, ClientModel.class);
    }

    /* ==========================================================
       ENTITY → MODEL (for API responses)
       ========================================================== */
    @Override
    public ClientModel toModel(ClientEntity entity) {
        ClientModel model = new ClientModel();
        model.setId(entity.getId());
        model.setDisplayName(entity.getDisplayName());
        model.setFirstName(entity.getFirstName());
        model.setSecondName(entity.getSecondName());
        model.setLastName(entity.getLastName());
        model.setSecondLastName(entity.getSecondLastName());
        model.setPrimaryEmail(entity.getPrimaryEmail());
        model.setPrimaryPhone(entity.getPrimaryPhone());
        model.setPrimaryAddress(entity.getPrimaryAddress());
        model.setActive(entity.getActive());

        // Convert LocalDateTime or Instant → OffsetDateTime (UTC)
        if (entity.getCreatedAt() != null)
            model.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (entity.getUpdatedAt() != null)
            model.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));

        // HATEOAS links
        model.add(linkTo(methodOn(ClientController.class).getById(entity.getId())).withSelfRel());
        model.add(linkTo(methodOn(ClientController.class).getAll(1, 9, null)).withRel("collection"));

        return model;
    }

    /* ==========================================================
       MODEL → ENTITY (for saves/updates)
       ========================================================== */
    public ClientEntity toEntity(ClientModel model) {
        ClientEntity entity = new ClientEntity();
        copyCommonFields(model, entity);
        return entity;
    }

    /* ==========================================================
       Partial update helper (PATCH / PUT)
       ========================================================== */
    public void updateEntityFromModel(ClientModel model, ClientEntity entity) {
        copyCommonFields(model, entity);
    }

    /* ==========================================================
       Shared mapper for reusable field copying
       ========================================================== */
    private void copyCommonFields(ClientModel model, ClientEntity entity) {
        if (model.getFirstName() != null) entity.setFirstName(model.getFirstName());
        if (model.getSecondName() != null) entity.setSecondName(model.getSecondName());
        if (model.getLastName() != null) entity.setLastName(model.getLastName());
        if (model.getSecondLastName() != null) entity.setSecondLastName(model.getSecondLastName());
        if (model.getPrimaryEmail() != null) entity.setPrimaryEmail(model.getPrimaryEmail());
        if (model.getPrimaryPhone() != null) entity.setPrimaryPhone(model.getPrimaryPhone());
        if (model.getPrimaryAddress() != null) entity.setPrimaryAddress(model.getPrimaryAddress());
        if (model.getActive() != null) entity.setActive(model.getActive());
    }
}
