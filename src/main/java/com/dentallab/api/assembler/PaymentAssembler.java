package com.dentallab.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.ZoneId;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.PaymentController;
import com.dentallab.api.model.PaymentModel;
import com.dentallab.persistence.entity.PaymentEntity;

@Component
public class PaymentAssembler
        extends RepresentationModelAssemblerSupport<PaymentEntity, PaymentModel> {

    public PaymentAssembler() {
        super(PaymentController.class, PaymentModel.class);
    }

    @Override
    public PaymentModel toModel(PaymentEntity entity) {

        PaymentModel model = new PaymentModel();

        model.setId(entity.getPaymentId());
        model.setClientId(entity.getClientId());
        model.setAmountTotal(entity.getAmountTotal());
        model.setCurrency(entity.getCurrency());
        model.setMethod(entity.getMethod());
        model.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        model.setReference(entity.getReference());
        model.setNotes(entity.getNotes());
        model.setReceivedAt(entity.getReceivedAt() != null ? entity.getReceivedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        model.setLastUpdated(entity.getLastUpdated() != null ? entity.getLastUpdated().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);

        model.add(linkTo(
                methodOn(PaymentController.class).getPaymentById(entity.getPaymentId())
        ).withSelfRel());

        model.add(linkTo(
                methodOn(PaymentController.class).getPaymentsByClient(entity.getClientId())
        ).withRel("client-payments"));

        return model;
    }
}
