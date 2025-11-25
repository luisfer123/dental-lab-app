package com.dentallab.api.assembler;

import com.dentallab.api.controller.ClientController;
import com.dentallab.api.model.ClientSummaryModel;
import com.dentallab.persistence.entity.ClientEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Converts ClientEntity ↔ ClientSummaryModel.
 * Lightweight version of ClientAssembler used for quick lookups,
 * nested references inside other resources, and search autocomplete.
 */
@Component
public class ClientSummaryAssembler
        extends RepresentationModelAssemblerSupport<ClientEntity, ClientSummaryModel> {

    public ClientSummaryAssembler() {
        super(ClientController.class, ClientSummaryModel.class);
    }

    /* ==========================================================
       ENTITY → SUMMARY MODEL
       ========================================================== */
    @Override
    public ClientSummaryModel toModel(ClientEntity entity) {

        ClientSummaryModel model = new ClientSummaryModel();

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

        // HATEOAS links
        model.add(linkTo(methodOn(ClientController.class)
                .getById(entity.getId())).withSelfRel());

        model.add(linkTo(methodOn(ClientController.class)
                .getAll(0, 10, null)).withRel("collection"));

        return model;
    }

    /* ==========================================================
       MODEL → ENTITY  (not typically used, but included for symmetry)
       ========================================================== */
    public ClientEntity toEntity(ClientSummaryModel model) {
        ClientEntity entity = new ClientEntity();
        copyCommonFields(model, entity);
        return entity;
    }

    /* ==========================================================
       UPDATE EXISTING ENTITY
       ========================================================== */
    public void updateEntityFromModel(ClientSummaryModel model, ClientEntity entity) {
        copyCommonFields(model, entity);
    }

    /* ==========================================================
       SHARED FIELD MAPPER
       ========================================================== */
    private void copyCommonFields(ClientSummaryModel model, ClientEntity entity) {

        if (model.getFirstName() != null)
            entity.setFirstName(model.getFirstName());

        if (model.getSecondName() != null)
            entity.setSecondName(model.getSecondName());

        if (model.getLastName() != null)
            entity.setLastName(model.getLastName());

        if (model.getSecondLastName() != null)
            entity.setSecondLastName(model.getSecondLastName());

        if (model.getPrimaryEmail() != null)
            entity.setPrimaryEmail(model.getPrimaryEmail());

        if (model.getPrimaryPhone() != null)
            entity.setPrimaryPhone(model.getPrimaryPhone());

        if (model.getPrimaryAddress() != null)
            entity.setPrimaryAddress(model.getPrimaryAddress());

        if (model.getActive() != null)
            entity.setActive(model.getActive());
    }
}
