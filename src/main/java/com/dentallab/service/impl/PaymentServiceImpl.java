package com.dentallab.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.PaymentAssembler;
import com.dentallab.api.model.PaymentCreateRequest;
import com.dentallab.api.model.PaymentModel;
import com.dentallab.domain.payment.model.PaymentAllocationRequest;
import com.dentallab.domain.payment.model.PaymentPreview;
import com.dentallab.domain.payment.service.PaymentPreviewService;
import com.dentallab.domain.payment.service.PaymentRegistrationService;
import com.dentallab.persistence.entity.PaymentEntity;
import com.dentallab.persistence.repository.PaymentRepository;
import com.dentallab.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentPreviewService paymentPreviewService;
    private final PaymentRegistrationService paymentRegistrationService;
    private final PaymentRepository paymentRepository;
    private final PaymentAssembler paymentAssembler;

    public PaymentServiceImpl(
            PaymentPreviewService paymentPreviewService,
            PaymentRegistrationService paymentRegistrationService,
            PaymentRepository paymentRepository,
            PaymentAssembler paymentAssembler
    ) {
        this.paymentPreviewService = paymentPreviewService;
        this.paymentRegistrationService = paymentRegistrationService;
        this.paymentRepository = paymentRepository;
        this.paymentAssembler = paymentAssembler;
    }

    @Override
    @Transactional
    public PaymentModel registerPayment(PaymentCreateRequest request) {

        // 1️⃣ Convert API allocations → domain allocations
        List<PaymentAllocationRequest> allocationRequests =
                request.getAllocations() == null
                        ? List.of()
                        : request.getAllocations().entrySet().stream()
                            .map(e -> new PaymentAllocationRequest(
                                    e.getKey(),
                                    e.getValue()
                            ))
                            .toList();

        // 2️⃣ Preview (domain validation + simulation)
        PaymentPreview preview = paymentPreviewService.preview(
                request.getClientId(),
                request.getAmountTotal(),
                allocationRequests
        );

        // 3️⃣ Register payment (authoritative, transactional)
        Long paymentId = paymentRegistrationService.registerPayment(
                request.getClientId(),
                request.getAmountTotal(),
                allocationRequests,
                request.getMethod(),
                request.getReference(),
                request.getNotes()
        );

        // 4️⃣ Load persisted payment and return API model
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalStateException("Payment not found after registration: " + paymentId)
                );

        return paymentAssembler.toModel(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentModel getPaymentById(Long paymentId) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found with id: " + paymentId)
                );

        return paymentAssembler.toModel(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentModel> getPaymentsByClient(Long clientId) {

        return paymentRepository.findByClientIdOrderByReceivedAtDesc(clientId)
                .stream()
                .map(paymentAssembler::toModel)
                .toList();
    }
}
