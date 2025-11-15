package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.ZoneOffset;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.DentistProfileController;
import com.dentallab.api.enums.ProfileType;
import com.dentallab.api.model.DentistProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.DentistProfileEntity;

/**
 * Assembles DentistProfileEntity ↔ DentistProfileModel with HATEOAS links.
 * Converts timestamps to OffsetDateTime (UTC) for API output.
 */
@Component
public class DentistProfileAssembler
        extends RepresentationModelAssemblerSupport<DentistProfileEntity, DentistProfileModel> {

    public DentistProfileAssembler() {
        super(DentistProfileController.class, DentistProfileModel.class);
    }

    // ============================================================
    // ENTITY → MODEL
    // ============================================================
    @Override
    public DentistProfileModel toModel(DentistProfileEntity entity) {
        DentistProfileModel model = new DentistProfileModel();
        model.setId(entity.getId());
        model.setType(ProfileType.DENTIST);

        if (entity.getClient() != null)
            model.setClientId(entity.getClient().getId());

        model.setClinicName(entity.getClinicName());

        // Convert Instant → OffsetDateTime (UTC)
        if (entity.getCreatedAt() != null)
            model.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (entity.getUpdatedAt() != null)
            model.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));

        // HATEOAS links
        model.add(linkTo(methodOn(DentistProfileController.class).getById(entity.getId())).withSelfRel());
        model.add(linkTo(methodOn(DentistProfileController.class).getAll()).withRel("collection"));

        return model;
    }

    // ============================================================
    // MODEL → ENTITY (CREATE)
    // ============================================================
    public DentistProfileEntity toEntity(DentistProfileModel model, ClientEntity client) {
        DentistProfileEntity entity = new DentistProfileEntity();
        entity.setClinicName(model.getClinicName());
        entity.setClient(client);
        return entity;
    }

    // ============================================================
    // Partial update (PATCH)
    // ============================================================
    public void updateEntityFromModel(DentistProfileModel model, DentistProfileEntity entity) {
        if (model.getClinicName() != null)
            entity.setClinicName(model.getClinicName());
    }
}