package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.stream.Collectors;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.WorkOrderController;
import com.dentallab.api.model.FullWorkOrderModel;
import com.dentallab.persistence.entity.WorkOrderEntity;

@Component
public class FullWorkOrderAssembler
        extends RepresentationModelAssemblerSupport<WorkOrderEntity, FullWorkOrderModel> {

    private final WorkAssembler workAssembler;

    public FullWorkOrderAssembler(WorkAssembler workAssembler) {
        super(WorkOrderController.class, FullWorkOrderModel.class);
        this.workAssembler = workAssembler;
    }

    @Override
    public FullWorkOrderModel toModel(WorkOrderEntity entity) {

        FullWorkOrderModel model = instantiateModel(entity);

        // ------------ Basic fields ------------
        model.setId(entity.getId());
        model.setDateReceived(entity.getDateReceived());
        model.setDueDate(entity.getDueDate());
        model.setDeliveredAt(entity.getDeliveredAt());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        model.setStatus(entity.getStatus());
        model.setNotes(entity.getNotes());

        // ------------ Client Info ------------
        if (entity.getClient() != null) {
            model.setClientId(entity.getClient().getId());
            model.setClientName(entity.getClient().getDisplayName());
            model.setClientPrimaryEmail(entity.getClient().getPrimaryEmail());
            model.setClientPrimaryPhone(entity.getClient().getPrimaryPhone());
        }

        // ------------ Embedded Works ------------
        if (entity.getWorks() != null) {
            model.setWorks(
                entity.getWorks().stream()
                    .map(workAssembler::toModel)
                    .collect(Collectors.toList())
            );
        }

        // ------------ HATEOAS Links ------------
        model.add(linkTo(
                    methodOn(WorkOrderController.class).getById(entity.getId()))
                    .withSelfRel()
        );

        model.add(linkTo(
                    methodOn(WorkOrderController.class).getByClientId(
                            entity.getClient().getId(), 0, 10, "createdAt,desc"))
                    .withRel("client-orders")
        );

        model.add(linkTo(
                    methodOn(WorkOrderController.class).getAll(0, 10, "createdAt,desc"))
                    .withRel("all-orders")
        );

        return model;
    }
}
