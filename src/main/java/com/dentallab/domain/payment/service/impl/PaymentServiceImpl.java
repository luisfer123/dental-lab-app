package com.dentallab.domain.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.enums.PaymentStatus;
import com.dentallab.domain.payment.dto.PaymentAllocationCommand;
import com.dentallab.domain.payment.dto.RegisterPaymentRequest;
import com.dentallab.domain.payment.query.WorkPaymentStatusQuery;
import com.dentallab.domain.payment.service.ClientBalanceService;
import com.dentallab.domain.payment.service.PaymentService;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkPricingService;
import com.dentallab.persistence.entity.PaymentAllocationEntity;
import com.dentallab.persistence.entity.PaymentEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.PaymentRepository;
import com.dentallab.persistence.repository.WorkRepository;

/**
 * Default implementation of {@link PaymentService}.
 *
 * <p>
 * Performs the <strong>authoritative commit</strong> of a payment.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 * <ul>
 *   <li>Persist payment header</li>
 *   <li>Persist payment allocations</li>
 *   <li>Delegate balance handling to {@link ClientBalanceService}</li>
 * </ul>
 *
 * <p>
 * All operations run inside a single transaction.
 * </p>
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final WorkRepository workRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final WorkPricingService workPricingService;
    private final WorkPaymentStatusQuery paymentStatusQuery;
    private final ClientBalanceService clientBalanceService;

    public PaymentServiceImpl(
            WorkRepository workRepository,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            WorkPricingService workPricingService,
            WorkPaymentStatusQuery paymentStatusQuery,
            ClientBalanceService clientBalanceService
    ) {
        this.workRepository = workRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.workPricingService = workPricingService;
        this.paymentStatusQuery = paymentStatusQuery;
        this.clientBalanceService = clientBalanceService;
    }

    @Override
    public void registerPayment(RegisterPaymentRequest request) {

    	Objects.requireNonNull(request, "request must not be null");
    	Objects.requireNonNull(request.getIdempotencyKey(), "idempotencyKey must not be null");
    	
    	if (request.getAllocations().isEmpty() && !request.isMoveRemainderToBalance()) {
    	    throw new IllegalArgumentException("Payment has no allocations and no balance destination");
    	}

    	log.info(
    	        "Registering payment: clientId={}, paymentAmount={}, idempotencyKey={}",
    	        request.getClientId(),
    	        request.getPaymentAmount(),
    	        request.getIdempotencyKey()
    	);

    	// ------------------------------------------------------------
    	// 0. Idempotency check (MUST be first)
    	// ------------------------------------------------------------
    	PaymentEntity existing =
    	        paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
    	                .orElse(null);

    	if (existing != null) {
    	    log.warn(
    	            "Idempotent payment request detected, returning existing payment: paymentId={}",
    	            existing.getId()
    	    );
    	    return; // <-- REAL early exit
    	}

    	BigDecimal paymentAmount = normalize(request.getPaymentAmount());


        // ------------------------------------------------------------
        // 1. Load and validate works
        // ------------------------------------------------------------
        List<Long> workIds =
                request.getAllocations().stream()
                        .map(PaymentAllocationCommand::getWorkId)
                        .toList();

        List<WorkEntity> works =
                workRepository.findByIdInAndClient_Id(
                        workIds,
                        request.getClientId()
                );

        if (works.size() != new HashSet<>(workIds).size()) {
            throw new IllegalArgumentException(
                    "Some works do not exist or do not belong to the client"
            );
        }

        // ------------------------------------------------------------
        // 2. Persist payment header
        // ------------------------------------------------------------
        PaymentEntity payment = new PaymentEntity();
        payment.setClientId(request.getClientId());
        payment.setReceivedAt(Instant.now());
        payment.setMethod(request.getMethod());
        payment.setReference(request.getReference());
        payment.setNotes(request.getNotes());
        payment.setAmountTotal(paymentAmount);
        payment.setStatus(PaymentStatus.RECEIVED);
        payment.setIdempotencyKey(request.getIdempotencyKey());

        try {
            paymentRepository.save(payment);
            log.debug("Payment persisted with id={}", payment.getId());
        } catch (DataIntegrityViolationException ex) {
            PaymentEntity existingRace =
                    paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                            .orElseThrow();
            log.warn("Idempotency race resolved, returning existing payment: {}", existingRace.getId());
            return;
        }

        // ------------------------------------------------------------
        // 3. Apply allocations (re-validate unpaid amounts)
        // ------------------------------------------------------------
        BigDecimal allocatedTotal = ZERO;

        for (PaymentAllocationCommand cmd : request.getAllocations()) {

            BigDecimal allocation = normalize(cmd.getAllocatedAmount());
            if (allocation.compareTo(ZERO) == 0) {
                continue;
            }

            PriceResolution price =
                    workPricingService.resolveFinalPrice(
                            PriceResolutionRequest.forWork(cmd.getWorkId())
                    );

            BigDecimal finalPrice = normalize(price.getFinalPrice());	

            BigDecimal alreadyPaid =
                    paymentStatusQuery.findCashPaidAmountsByWorkIds(
                            Set.of(cmd.getWorkId())
                    ).getOrDefault(cmd.getWorkId(), ZERO)
                    .add(
                        paymentStatusQuery.findBalancePaidAmountsByWorkIds(
                                Set.of(cmd.getWorkId())
                        ).getOrDefault(cmd.getWorkId(), ZERO)
                    );

            BigDecimal unpaid = normalize(finalPrice.subtract(alreadyPaid));

            if (allocation.compareTo(unpaid) > 0) {
                throw new IllegalStateException(
                        "Allocation exceeds unpaid amount for workId=" + cmd.getWorkId()
                );
            }

            PaymentAllocationEntity pa = new PaymentAllocationEntity();
            pa.setPaymentId(payment.getId());
            pa.setWorkId(cmd.getWorkId());
            pa.setAmountApplied(allocation);

            paymentAllocationRepository.save(pa);

            allocatedTotal = normalize(allocatedTotal.add(allocation));

            log.debug(
                    "Applied allocation: paymentId={}, workId={}, amount={}",
                    payment.getId(),
                    cmd.getWorkId(),
                    allocation
            );
        }
        
        if (allocatedTotal.compareTo(paymentAmount) > 0) {
            throw new IllegalStateException(
                    "Allocated total exceeds payment amount"
            );
        }

        // ------------------------------------------------------------
        // 4. Handle remaining amount → client balance
        // ------------------------------------------------------------
        BigDecimal remainder =
                normalize(paymentAmount.subtract(allocatedTotal));

        if (remainder.compareTo(ZERO) > 0) {

            if (!request.isMoveRemainderToBalance()) {
                throw new IllegalStateException(
                        "Remaining amount exists but balance confirmation was not provided"
                );
            }

            clientBalanceService.creditBalance(
                    request.getClientId(),
                    remainder,
                    "PAY_EXCESS",
                    payment.getId(),
                    null,
                    "Excess payment credited to client balance"
            );

            log.info(
                    "Balance credited from payment remainder: clientId={}, amount={}",
                    request.getClientId(),
                    remainder
            );
        }

        log.info(
                "Payment registration completed: paymentId={}, allocated={}, remainder={}",
                payment.getId(),
                allocatedTotal,
                remainder
        );
    }

    /**
     * Normalizes monetary values.
     */
    private static BigDecimal normalize(BigDecimal v) {
        return v == null ? ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}
