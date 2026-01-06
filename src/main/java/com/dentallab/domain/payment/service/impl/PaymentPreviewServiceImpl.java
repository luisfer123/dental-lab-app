package com.dentallab.domain.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.payment.dto.PaymentPreviewRequest;
import com.dentallab.domain.payment.dto.PaymentPreviewResult;
import com.dentallab.domain.payment.dto.WorkAllocationPreview;
import com.dentallab.domain.payment.query.WorkPaymentStatusQuery;
import com.dentallab.domain.payment.service.PaymentPreviewService;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkPricingService;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.WorkRepository;

/**
 * <p>
 * Default implementation of {@link PaymentPreviewService}.
 * </p>
 *
 * <p>
 * This service is responsible for computing a <strong>read-only preview</strong>
 * of how a payment would be allocated across a set of works for a given client.
 * </p>
 *
 * <h3>Key characteristics</h3>
 *
 * <ul>
 *   <li>Purely read-only (no persistence, no side effects)</li>
 *   <li>Deterministic: same input state → same output</li>
 *   <li>Delegates pricing to {@link WorkPricingService}</li>
 *   <li>Delegates payment aggregation to {@link WorkPaymentStatusQuery}</li>
 * </ul>
 *
 * <p>
 * This class deliberately performs <strong>no inference or guessing</strong>.
 * All results are made explicit in {@link PaymentPreviewResult}, including
 * unallocated remainders and required confirmations.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class PaymentPreviewServiceImpl implements PaymentPreviewService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentPreviewServiceImpl.class);

    /**
     * Canonical zero value for monetary computations.
     * Always scaled to 2 decimals to avoid propagation of scale mismatches.
     */
    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final WorkRepository workRepository;
    private final WorkPricingService workPricingService;
    private final WorkPaymentStatusQuery paymentStatusQuery;

    public PaymentPreviewServiceImpl(
            WorkRepository workRepository,
            WorkPricingService workPricingService,
            WorkPaymentStatusQuery paymentStatusQuery
    ) {
        this.workRepository = workRepository;
        this.workPricingService = workPricingService;
        this.paymentStatusQuery = paymentStatusQuery;
    }

    /**
     * <p>
     * Computes a preview of how the given payment would be allocated
     * across the selected works.
     * </p>
     *
     * <p>
     * This method enforces the following invariants:
     * </p>
     *
     * <ul>
     *   <li>All selected works must exist</li>
     *   <li>All selected works must belong to the given client</li>
     *   <li>No work can receive more than its unpaid amount</li>
     *   <li>No allocation can exceed the remaining payment amount</li>
     * </ul>
     *
     * <p>
     * Violations of these invariants result in an exception, as they
     * indicate either invalid input or a workflow error.
     * </p>
     */
    @Override
    public PaymentPreviewResult preview(PaymentPreviewRequest request) {

        Objects.requireNonNull(request, "request must not be null");
        
        if (request.getSelectedWorkIds().isEmpty()) {
            log.warn("Payment preview requested with no selected works");
        }

        log.debug(
                "Starting payment preview: clientId={}, paymentAmount={}, selectedWorkIds={}",
                request.getClientId(),
                request.getPaymentAmount(),
                request.getSelectedWorkIds()
        );

        // ------------------------------------------------------------
        // 1. Load works and enforce ownership
        // ------------------------------------------------------------
        List<WorkEntity> works =
                workRepository.findByIdInAndClient_Id(
                        request.getSelectedWorkIds(),
                        request.getClientId()
                );

        // If the number of loaded works does not match the number of requested IDs,
        // then at least one work either does not exist or belongs to another client.
        if (works.size() != new HashSet<>(request.getSelectedWorkIds()).size()) {
            log.warn(
                    "Payment preview rejected: ownership or existence violation " +
                    "(clientId={}, requestedWorks={}, loadedWorks={})",
                    request.getClientId(),
                    request.getSelectedWorkIds().size(),
                    works.size()
            );
            throw new IllegalArgumentException(
                    "Some works do not exist or do not belong to the client"
            );
        }

        // Deterministic ordering: allocations are applied in a stable order.
        works.sort(Comparator.comparing(WorkEntity::getId));

        // ------------------------------------------------------------
        // 2. Load already-paid amounts (cash + balance)
        // ------------------------------------------------------------
        Map<Long, BigDecimal> cashPaid =
                paymentStatusQuery.findCashPaidAmountsByWorkIds(
                        request.getSelectedWorkIds()
                );

        Map<Long, BigDecimal> balancePaid =
                paymentStatusQuery.findBalancePaidAmountsByWorkIds(
                        request.getSelectedWorkIds()
                );

        BigDecimal remaining = normalize(request.getPaymentAmount());

        List<WorkAllocationPreview> previews = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        BigDecimal totalAllocated = ZERO;
        BigDecimal totalUnpaid = ZERO;

        // ------------------------------------------------------------
        // 3. Allocation loop (Option A: automatic allocation)
        // ------------------------------------------------------------
        for (WorkEntity work : works) {

            log.debug("Resolving price for workId={}", work.getId());

            PriceResolution price =
                    workPricingService.resolveFinalPrice(
                            PriceResolutionRequest.forWork(work.getId())
                    );

            BigDecimal finalPrice = normalize(price.getFinalPrice());

            BigDecimal alreadyPaid =
                    normalize(
                            cashPaid.getOrDefault(work.getId(), ZERO)
                                    .add(balancePaid.getOrDefault(work.getId(), ZERO))
                    );

            BigDecimal unpaid = normalize(finalPrice.subtract(alreadyPaid));
            if (unpaid.compareTo(ZERO) < 0) {
                // Defensive clamp; should not occur if pricing/payment invariants hold.
                unpaid = ZERO;
            }

            totalUnpaid = normalize(totalUnpaid.add(unpaid));

            BigDecimal allocated = ZERO;

	         // Allocate as much as possible, but never more than:
	         // - remaining payment
	         // - unpaid amount of this work
	         if (remaining.compareTo(ZERO) > 0 && unpaid.compareTo(ZERO) > 0) {
	             allocated = normalize(unpaid.min(remaining));
	             remaining = normalize(remaining.subtract(allocated));
	             totalAllocated = normalize(totalAllocated.add(allocated));
	         }


            log.debug(
                    "Work allocation preview: workId={}, finalPrice={}, alreadyPaid={}, unpaid={}, allocated={}",
                    work.getId(),
                    finalPrice,
                    alreadyPaid,
                    unpaid,
                    allocated
            );

            WorkAllocationPreview wap = new WorkAllocationPreview();
            wap.setWorkId(work.getId());
            wap.setWorkLabel(workLabel(work));
            wap.setWorkPrice(finalPrice);
            wap.setAlreadyPaidAmount(alreadyPaid);
            wap.setUnpaidAmount(unpaid);
            wap.setMaxAllocatableAmount(unpaid);
            wap.setAllocatedAmount(allocated);

            previews.add(wap);
        }

        // ------------------------------------------------------------
        // 4. Assemble result
        // ------------------------------------------------------------
        PaymentPreviewResult result = new PaymentPreviewResult();
        result.setClientId(request.getClientId());
        result.setPaymentAmount(request.getPaymentAmount());
        result.setWorkAllocations(previews);
        result.setTotalUnpaidSelected(totalUnpaid);
        result.setTotalAllocated(totalAllocated);
        result.setRemainingUnallocated(remaining);
        result.setRequiresBalanceConfirmation(remaining.compareTo(ZERO) > 0);
        result.setWarnings(warnings);

        if (remaining.compareTo(ZERO) > 0) {
            log.info(
                    "Payment preview requires balance confirmation: remainingUnallocated={}",
                    remaining
            );
        }

        // TODO: implement unpaid-work suggestions when remaining > 0
        //       (non-blocking enhancement; preview logic is complete without it)

        log.debug(
                "Payment preview completed: totalAllocated={}, remainingUnallocated={}",
                totalAllocated,
                remaining
        );

        return result;
    }

    /**
     * Normalizes monetary values to a canonical scale.
     */
    private static BigDecimal normalize(BigDecimal v) {
        return v == null ? ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Produces a human-readable label for a work.
     *
     * TODO: Replace with richer labeling logic once available
     *       (e.g. type + internal sequence, order reference, etc.).
     */
    private static String workLabel(WorkEntity work) {
        return "Work " + work.getId();
    }
}
