package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.ZoneOffset;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.TechnicianProfileController;
import com.dentallab.api.enums.ProfileType;
import com.dentallab.api.model.TechnicianProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.TechnicianProfileEntity;

/**
 * Assembles TechnicianProfileEntity ↔ TechnicianProfileModel with HATEOAS links.
 * Converts timestamps to OffsetDateTime (UTC) for API output.
 */
@Component
public class TechnicianProfileAssembler
        extends RepresentationModelAssemblerSupport<TechnicianProfileEntity, TechnicianProfileModel> {

    public TechnicianProfileAssembler() {
        super(TechnicianProfileController.class, TechnicianProfileModel.class);
    }

    // ============================================================
    // ENTITY → MODEL
    // ============================================================
    @Override
    public TechnicianProfileModel toModel(TechnicianProfileEntity entity) {
        TechnicianProfileModel model = new TechnicianProfileModel();
        model.setId(entity.getId());
        model.setType(ProfileType.TECHNICIAN);

        // Client ID (if available)
        if (entity.getClient() != null)
            model.setClientId(entity.getClient().getId());

        // Basic fields
        model.setLabName(entity.getLabName());
        model.setSpecialization(entity.getSpecialization());
        model.setActive(entity.getActive());

        // Convert Instant → OffsetDateTime (UTC)
        if (entity.getCreatedAt() != null)
            model.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (entity.getUpdatedAt() != null)
            model.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));

        // HATEOAS links
        model.add(linkTo(methodOn(TechnicianProfileController.class).getById(entity.getId())).withSelfRel());
        model.add(linkTo(methodOn(TechnicianProfileController.class).getAll(0, 10, null)).withRel("collection"));

        return model;
    }

    // ============================================================
    // MODEL → ENTITY (CREATE)
    // ============================================================
    public TechnicianProfileEntity toEntity(TechnicianProfileModel model, ClientEntity client) {
        TechnicianProfileEntity entity = new TechnicianProfileEntity();
        entity.setLabName(model.getLabName());
        entity.setSpecialization(model.getSpecialization());
        entity.setActive(model.getActive() != null ? model.getActive() : Boolean.TRUE);
        entity.setClient(client);
        return entity;
    }

    // ============================================================
    // Partial update (PATCH)
    // ============================================================
    public void updateEntityFromModel(TechnicianProfileModel model, TechnicianProfileEntity entity) {
        if (model.getLabName() != null)
            entity.setLabName(model.getLabName());
        if (model.getSpecialization() != null)
            entity.setSpecialization(model.getSpecialization());
        if (model.getActive() != null)
            entity.setActive(model.getActive());
    }
}
