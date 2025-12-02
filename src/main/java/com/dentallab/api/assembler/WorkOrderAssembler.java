package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.stream.Collectors;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.WorkOrderController;
import com.dentallab.api.model.WorkOrderModel;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;

@Component
public class WorkOrderAssembler extends RepresentationModelAssemblerSupport<WorkOrderEntity, WorkOrderModel> {

    public WorkOrderAssembler() {
        super(WorkOrderController.class, WorkOrderModel.class);
    }

    @Override
    public WorkOrderModel toModel(WorkOrderEntity entity) {

        WorkOrderModel model = instantiateModel(entity);

        model.setId(entity.getId());
        model.setClientId(entity.getClient().getId());
        model.setClientName(entity.getClient().getDisplayName());

        model.setDateReceived(entity.getDateReceived());
        model.setDueDate(entity.getDueDate());
        model.setDeliveredAt(entity.getDeliveredAt());

        model.setStatus(entity.getStatus());
        model.setNotes(entity.getNotes());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getWorks() != null) {
            model.setWorkIds(
                entity.getWorks().stream()
                        .map(WorkEntity::getId)
                        .collect(Collectors.toList())
            );
        }

        // ---------- HATEOAS Links ----------
        model.add(linkTo(methodOn(WorkOrderController.class).getById(entity.getId()))
                .withSelfRel());

        model.add(linkTo(methodOn(WorkOrderController.class)
                .getByClientId(entity.getClient().getId(), 0, 10, "createdAt,desc"))
                .withRel("client-orders"));

        model.add(linkTo(methodOn(WorkOrderController.class).getAll(0, 10, "createdAt,desc"))
                .withRel("all-orders"));

        return model;
    }
}
