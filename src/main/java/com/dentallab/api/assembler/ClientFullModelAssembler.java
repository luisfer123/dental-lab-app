package com.dentallab.api.assembler;

import com.dentallab.api.model.ClientFullModel;
import com.dentallab.api.model.ProfileModel;
import com.dentallab.domain.profile.ProfileRegistry;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.api.controller.ClientController;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClientFullModelAssembler implements RepresentationModelAssembler<ClientEntity, ClientFullModel> {

    private final ProfileRegistry profileRegistry;

    public ClientFullModelAssembler(ProfileRegistry profileRegistry) {
        this.profileRegistry = profileRegistry;
    }

    @Override
    public ClientFullModel toModel(ClientEntity e) {
        ClientFullModel m = new ClientFullModel();

        m.setId(e.getId());
        m.setDisplayName(e.getDisplayName());
        m.setFirstName(e.getFirstName());
        m.setSecondName(e.getSecondName());
        m.setLastName(e.getLastName());
        m.setSecondLastName(e.getSecondLastName());
        m.setPrimaryEmail(e.getPrimaryEmail());
        m.setPrimaryPhone(e.getPrimaryPhone());
        m.setPrimaryAddress(e.getPrimaryAddress());
        m.setActive(e.getActive());

        OffsetDateTime created = e.getCreatedAt() != null ? e.getCreatedAt().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime updated = e.getUpdatedAt() != null ? e.getUpdatedAt().atOffset(ZoneOffset.UTC) : null;
        m.setCreatedAt(created);
        m.setUpdatedAt(updated);

        List<ProfileModel> profiles = profileRegistry.loadProfiles(e.getId());
        profiles.forEach(m::addProfile);

        Link self = linkTo(methodOn(ClientController.class).getClientFull(e.getId())).withSelfRel();
        m.add(self);

        // Podrías añadir aquí más links útiles (casos, facturas, etc.)
        return m;
    }
}
