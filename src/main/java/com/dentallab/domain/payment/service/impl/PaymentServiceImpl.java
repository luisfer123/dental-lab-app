package com.dentallab.domain.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    	    return;
    	}

    	// ------------------------------------------------------------
    	// Normalize allocations AFTER idempotency
    	// ------------------------------------------------------------
    	List<PaymentAllocationCommand> allocations =
    	        Objects.requireNonNullElse(request.getAllocations(), List.of());

    	if (allocations.isEmpty() && !request.isMoveRemainderToBalance()) {
    	    throw new IllegalArgumentException(
    	        "Payment has no allocations and no balance destination"
    	    );
    	}

    	// 🔴 EARLY validation — no DB access
    	for (PaymentAllocationCommand cmd : allocations) {
    	    BigDecimal allocation = normalize(cmd.getAllocatedAmount());
    	    if (allocation.compareTo(ZERO) < 0) {
    	        throw new IllegalArgumentException("Negative allocation not allowed");
    	    }
    	}

    	BigDecimal paymentAmount = normalize(request.getPaymentAmount());
    	
    	if (paymentAmount.compareTo(ZERO) <= 0) {
    	    throw new IllegalArgumentException("Payment amount must be positive");
    	}

        // ------------------------------------------------------------
        // 1. Load and validate works
        // ------------------------------------------------------------
    	List<PaymentAllocationCommand> effectiveAllocations =
    		    allocations.stream()
    		        .filter(a -> normalize(a.getAllocatedAmount()).compareTo(ZERO) > 0)
    		        .toList();
    	
    	if (effectiveAllocations.isEmpty()) {

    	    log.info(
    	        "Payment has no effective allocations (all zero). Entire amount will be treated as remainder. paymentAmount={}, moveToBalance={}",
    	        paymentAmount,
    	        request.isMoveRemainderToBalance()
    	    );

    	    if (!request.isMoveRemainderToBalance()) {
    	        throw new IllegalStateException(
    	            "Payment has no effective allocations and remainder was not approved for balance"
    	        );
    	    }

    	    // Skip work loading, pricing, allocations entirely
    	    persistPaymentAndCreditBalanceOnly(request, paymentAmount);
    	    return;
    	}

    		List<Long> workIds =
    		    effectiveAllocations.stream()
    		        .map(PaymentAllocationCommand::getWorkId)
    		        .toList();
        
        if (new HashSet<>(workIds).size() != workIds.size()) {
            throw new IllegalArgumentException("Duplicate work allocations are not allowed");
        }

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
        
        Map<Long, PriceResolution> finalPricesByWorkId =
        	    resolveFinalPricesForWorks(
        	        effectiveAllocations.stream()
        	            .map(PaymentAllocationCommand::getWorkId)
        	            .toList()
        	    );
        
        for(PaymentAllocationCommand alloc : effectiveAllocations) {
        	if(alloc.getAllocatedAmount()
        			.compareTo(finalPricesByWorkId.get(alloc.getWorkId()).getFinalPrice()) > 0) {
        		throw new IllegalArgumentException(
						"Allocation exceeds work price: workId=" + alloc.getWorkId()
						);
        	}
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
        
        Set<Long> workIdsSet =
        	    effectiveAllocations.stream()
        	        .map(PaymentAllocationCommand::getWorkId)
        	        .collect(java.util.stream.Collectors.toSet());

        Map<Long, BigDecimal> cashPaid =
                paymentStatusQuery.findCashPaidAmountsByWorkIds(workIdsSet);

        Map<Long, BigDecimal> balancePaid =
                paymentStatusQuery.findBalancePaidAmountsByWorkIds(workIdsSet);


        for (PaymentAllocationCommand cmd : effectiveAllocations) {

            BigDecimal allocation = normalize(cmd.getAllocatedAmount());

            // 🔵 (UNCHANGED) skip zero allocations defensively
            if (allocation.compareTo(ZERO) == 0) {
                log.debug(
                    "Skipping zero allocation: workId={}, paymentId={}",
                    cmd.getWorkId(),
                    payment.getId()
                );
                continue;
            }

            // 🔴 (CHANGED) defensive lookup + explicit error
            PriceResolution price = finalPricesByWorkId.get(cmd.getWorkId());
            if (price == null) {
                throw new IllegalStateException(
                    "Final price not resolved for workId=" + cmd.getWorkId()
                );
            }

            BigDecimal finalPrice = normalize(price.getFinalPrice());

            // 🔴 (CHANGED) compute already-paid using normalized defaults
            BigDecimal alreadyPaid =
                    normalize(
                        cashPaid.getOrDefault(cmd.getWorkId(), ZERO)
                            .add(balancePaid.getOrDefault(cmd.getWorkId(), ZERO))
                    );

            BigDecimal unpaid = normalize(finalPrice.subtract(alreadyPaid));

            log.debug(
                "Validating allocation: paymentId={}, workId={}, allocation={}, finalPrice={}, alreadyPaid={}, unpaid={}",
                payment.getId(),
                cmd.getWorkId(),
                allocation,
                finalPrice,
                alreadyPaid,
                unpaid
            );

            // 🔴 (UNCHANGED logic, but now logged)
            if (allocation.compareTo(unpaid) > 0) {
                throw new IllegalStateException(
                    "Allocation exceeds unpaid amount for workId=" + cmd.getWorkId()
                );
            }

            // 🔵 (UNCHANGED) persist allocation
            PaymentAllocationEntity pa = new PaymentAllocationEntity();
            pa.setPayment(payment);
            pa.setWork(
                works.stream()
                     .filter(w -> w.getId().equals(cmd.getWorkId()))
                     .findFirst()
                     .orElseThrow(() ->
                         new IllegalStateException(
                             "Work not loaded for allocation: workId=" + cmd.getWorkId()
                         )
                     )
            );
            pa.setAmountApplied(allocation);

            paymentAllocationRepository.save(pa);

            allocatedTotal = normalize(allocatedTotal.add(allocation));

            log.info(
                "Applied allocation successfully: paymentId={}, workId={}, amount={}",
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
    
    private Map<Long, PriceResolution> resolveFinalPricesForWorks(List<Long> workIds) {
        return workIds.stream()
            .distinct()
            .collect(
                java.util.stream.Collectors.toMap(
                    id -> id,
                    id -> {
                        PriceResolution pr =
                            workPricingService.resolveFinalPrice(
                                PriceResolutionRequest.forWork(id)
                            );

                        if (pr == null) {
                            throw new IllegalStateException(
                                "Final price resolution returned null for workId=" + id
                            );
                        }
                        return pr;
                    }
                )
            );
    }
    
    private void persistPaymentAndCreditBalanceOnly(RegisterPaymentRequest request,
            BigDecimal amount) {

		PaymentEntity payment = new PaymentEntity();
		payment.setClientId(request.getClientId());
		payment.setReceivedAt(Instant.now());
		payment.setMethod(request.getMethod());
		payment.setReference(request.getReference());
		payment.setNotes(request.getNotes());
		payment.setAmountTotal(amount);
		payment.setStatus(PaymentStatus.RECEIVED);
		payment.setIdempotencyKey(request.getIdempotencyKey());
		
		paymentRepository.save(payment);
		
		clientBalanceService.creditBalance(
			request.getClientId(),
			amount,
			"PAY_EXCESS",
			payment.getId(),
			null,
			"Payment with no allocations credited entirely to balance"
			);
			
			log.info(
				"Payment with no allocations completed: paymentId={}, creditedToBalance={}",
				payment.getId(),
				amount
		);
	}

}
