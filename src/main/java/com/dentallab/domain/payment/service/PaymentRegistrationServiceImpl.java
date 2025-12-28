package com.dentallab.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.payment.model.PaymentAllocationRequest;
import com.dentallab.domain.payment.model.PaymentPreview;
import com.dentallab.domain.payment.query.ClientBalanceQuery;
import com.dentallab.persistence.entity.ClientBalanceEntity;
import com.dentallab.persistence.entity.ClientBalanceMovementEntity;
import com.dentallab.persistence.entity.PaymentAllocationEntity;
import com.dentallab.persistence.entity.PaymentEntity;
import com.dentallab.persistence.repository.ClientBalanceMovementRepository;
import com.dentallab.persistence.repository.ClientBalanceRepository;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.PaymentRepository;

@Service
@Transactional
public class PaymentRegistrationServiceImpl implements PaymentRegistrationService {

    private final PaymentPreviewService paymentPreviewService;

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final ClientBalanceRepository clientBalanceRepository;
    private final ClientBalanceMovementRepository balanceMovementRepository;

    private final ClientBalanceQuery clientBalanceQuery;

    public PaymentRegistrationServiceImpl(
            PaymentPreviewService paymentPreviewService,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository allocationRepository,
            ClientBalanceRepository clientBalanceRepository,
            ClientBalanceMovementRepository balanceMovementRepository,
            ClientBalanceQuery clientBalanceQuery) {

        this.paymentPreviewService = paymentPreviewService;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.clientBalanceRepository = clientBalanceRepository;
        this.balanceMovementRepository = balanceMovementRepository;
        this.clientBalanceQuery = clientBalanceQuery;
    }

    @Override
    public Long registerPayment(Long clientId,
                                BigDecimal paymentAmount,
                                List<PaymentAllocationRequest> allocations,
                                String method,
                                String reference,
                                String notes) {

        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(paymentAmount, "paymentAmount must not be null");

        // 1️⃣ Re-run preview inside the transaction (trust but verify)
        PaymentPreview preview = paymentPreviewService.preview(
                clientId,
                paymentAmount,
                allocations
        );

        // 2️⃣ Persist payment
        PaymentEntity payment = new PaymentEntity();
        payment.setClientId(clientId);
        payment.setAmountTotal(paymentAmount);
        payment.setMethod(method);
        payment.setReference(reference);
        payment.setNotes(notes);

        payment = paymentRepository.save(payment);

        // 3️⃣ Persist allocations
        for (PaymentAllocationRequest req : allocations) {
            if (req.getAmount().signum() > 0) {
                PaymentAllocationEntity allocation = new PaymentAllocationEntity();
                allocation.setPaymentId(payment.getPaymentId());
                allocation.setWorkId(req.getWorkId());
                allocation.setAmountApplied(req.getAmount());

                allocationRepository.save(allocation);

                // Ledger: money used to pay a work
                ClientBalanceMovementEntity movement = new ClientBalanceMovementEntity();
                movement.setClientId(clientId);
                movement.setAmountChange(req.getAmount().negate());
                movement.setType("APPLY_WORK");
                movement.setPaymentId(payment.getPaymentId());
                movement.setWorkId(req.getWorkId());
                movement.setNote("Applied payment to work");

                balanceMovementRepository.save(movement);
            }
        }

        // 4️⃣ Handle remaining amount → client balance
        BigDecimal remaining = preview.getRemainingUnallocated();

        if (remaining.signum() > 0) {

            ClientBalanceMovementEntity movement = new ClientBalanceMovementEntity();
            movement.setClientId(clientId);
            movement.setAmountChange(remaining);
            movement.setType("PAY_EXCESS");
            movement.setPaymentId(payment.getPaymentId());
            movement.setNote("Payment excess credited to client balance");

            balanceMovementRepository.save(movement);

            // Update cached client balance
            ClientBalanceEntity balance = clientBalanceRepository
                    .findByClientId(clientId)
                    .orElseGet(() -> {
                        ClientBalanceEntity b = new ClientBalanceEntity();
                        b.setClientId(clientId);
                        b.setAmount(BigDecimal.ZERO);
                        b.setActive(true);
                        return b;
                    });

            balance.setAmount(balance.getAmount().add(remaining));
            clientBalanceRepository.save(balance);
        }

        return payment.getPaymentId();
    }
}
