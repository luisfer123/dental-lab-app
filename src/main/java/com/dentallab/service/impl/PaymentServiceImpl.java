package com.dentallab.service.impl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.PaymentAssembler;
import com.dentallab.api.model.PaymentCreateRequest;
import com.dentallab.api.model.PaymentModel;
import com.dentallab.domain.enums.PaymentStatus;
import com.dentallab.persistence.entity.ClientBalanceEntity;
import com.dentallab.persistence.entity.ClientBalanceMovementEntity;
import com.dentallab.persistence.entity.PaymentAllocationEntity;
import com.dentallab.persistence.entity.PaymentEntity;
import com.dentallab.persistence.repository.ClientBalanceMovementRepository;
import com.dentallab.persistence.repository.ClientBalanceRepository;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.PaymentRepository;
import com.dentallab.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final ClientBalanceRepository clientBalanceRepository;
    private final ClientBalanceMovementRepository clientBalanceMovementRepository;
    private final PaymentAssembler paymentAssembler;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            ClientBalanceRepository clientBalanceRepository,
            ClientBalanceMovementRepository clientBalanceMovementRepository,
            PaymentAssembler paymentAssembler
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.clientBalanceRepository = clientBalanceRepository;
        this.clientBalanceMovementRepository = clientBalanceMovementRepository;
        this.paymentAssembler = paymentAssembler;
    }

    @Override
    @Transactional
    public PaymentModel registerPayment(PaymentCreateRequest request) {

        // ------------------------------------------------------
        // 1) Create and persist Payment
        // ------------------------------------------------------

        PaymentEntity payment = new PaymentEntity();
        payment.setClientId(request.getClientId());
        payment.setAmountTotal(request.getAmountTotal());
        payment.setCurrency(request.getCurrency());
        payment.setMethod(request.getMethod());
        payment.setReference(request.getReference());
        payment.setNotes(request.getNotes());
        payment.setStatus(PaymentStatus.RECEIVED);
        payment.setReceivedAt(OffsetDateTime.now());
        payment.setLastUpdated(OffsetDateTime.now());

        payment = paymentRepository.save(payment);

        // ------------------------------------------------------
        // 2) Apply allocations
        // ------------------------------------------------------

        BigDecimal totalAllocated = BigDecimal.ZERO;

        if (request.getAllocations() != null) {
            for (Map.Entry<Long, BigDecimal> entry : request.getAllocations().entrySet()) {

                Long workId = entry.getKey();
                BigDecimal amount = entry.getValue();

                if (amount == null || amount.signum() <= 0) {
                    continue;
                }

                PaymentAllocationEntity allocation = new PaymentAllocationEntity();
                allocation.setPaymentId(payment.getPaymentId());
                allocation.setWorkId(workId);
                allocation.setAmountApplied(amount);
                allocation.setCreatedAt(OffsetDateTime.now());

                paymentAllocationRepository.save(allocation);

                totalAllocated = totalAllocated.add(amount);
            }
        }

        // ------------------------------------------------------
        // 3) Validate allocation does not exceed payment
        // ------------------------------------------------------

        if (totalAllocated.compareTo(payment.getAmountTotal()) > 0) {
            throw new IllegalStateException(
                    "Total allocation exceeds payment amount"
            );
        }

        // ------------------------------------------------------
        // 4) Handle excess payment → client balance
        // ------------------------------------------------------

        BigDecimal excess = payment.getAmountTotal().subtract(totalAllocated);
        
        Long clientId = payment.getClientId();
        String currency = payment.getCurrency();

        if (excess.signum() > 0) {

            ClientBalanceEntity balance = clientBalanceRepository
                    .findByClientId(payment.getClientId())
                    .orElseGet(() -> {
                    	ClientBalanceEntity b = new ClientBalanceEntity();
                        b.setClientId(clientId);
                        b.setAmount(BigDecimal.ZERO);
                        b.setCurrency(currency);
                        b.setActive(true);
                        b.setUpdatedAt(OffsetDateTime.now());
                        return clientBalanceRepository.save(b);
                    });

            if (balance.isActive()) {

                // balance movement
                ClientBalanceMovementEntity movement = new ClientBalanceMovementEntity();
                movement.setClientId(payment.getClientId());
                movement.setAmountChange(excess);
                movement.setCurrency(payment.getCurrency());
                movement.setType("PAY_EXCESS");
                movement.setPaymentId(payment.getPaymentId());
                movement.setCreatedAt(OffsetDateTime.now());
                movement.setNote("Excess payment added to client balance");

                clientBalanceMovementRepository.save(movement);

                // update balance cache
                balance.setAmount(balance.getAmount().add(excess));
                balance.setUpdatedAt(OffsetDateTime.now());

                clientBalanceRepository.save(balance);
            }
        }

        // ------------------------------------------------------
        // 5) Return model
        // ------------------------------------------------------

        return paymentAssembler.toModel(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentModel getPaymentById(Long paymentId) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found with id: " + paymentId
                ));

        return paymentAssembler.toModel(payment);
    }@Override
    @Transactional(readOnly = true)
    public List<PaymentModel> getPaymentsByClient(Long clientId) {

        List<PaymentEntity> payments =
                paymentRepository.findByClientIdOrderByReceivedAtDesc(clientId);

        return payments.stream()
                .map(paymentAssembler::toModel)
                .collect(Collectors.toList());
    }


}
