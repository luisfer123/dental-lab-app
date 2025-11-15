package com.dentallab.api.assembler;

import com.dentallab.api.controller.StudentProfileController;
import com.dentallab.api.model.StudentProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.StudentProfileEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Converts StudentProfileEntity ↔ StudentProfileModel and adds HATEOAS links.
 */
@Component
public class StudentProfileAssembler extends RepresentationModelAssemblerSupport<StudentProfileEntity, StudentProfileModel> {

    public StudentProfileAssembler() {
        super(StudentProfileController.class, StudentProfileModel.class);
    }

    /* ENTITY → MODEL */
    @Override
    public StudentProfileModel toModel(StudentProfileEntity entity) {
        StudentProfileModel model = new StudentProfileModel();
        model.setId(entity.getId());
        if (entity.getClient() != null)
            model.setClientId(entity.getClient().getId());
        model.setUniversityName(entity.getUniversityName());
        model.setSemester(entity.getSemester());

        if (entity.getCreatedAt() != null)
            model.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (entity.getUpdatedAt() != null)
            model.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));

        model.add(linkTo(methodOn(StudentProfileController.class).getById(entity.getId())).withSelfRel());
        model.add(linkTo(methodOn(StudentProfileController.class).getAll()).withRel("students"));
        return model;
    }

    /* MODEL → ENTITY */
    public StudentProfileEntity toEntity(StudentProfileModel model, ClientEntity client) {
        StudentProfileEntity entity = new StudentProfileEntity();
        entity.setClient(client);
        entity.setUniversityName(model.getUniversityName());
        entity.setSemester(model.getSemester());
        return entity;
    }

    /* Partial update helper */
    public void updateEntityFromModel(StudentProfileModel model, StudentProfileEntity entity) {
        entity.setUniversityName(model.getUniversityName());
        entity.setSemester(model.getSemester());
    }
}