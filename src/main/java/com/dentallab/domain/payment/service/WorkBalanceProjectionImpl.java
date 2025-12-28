package com.dentallab.domain.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.payment.model.WorkBalance;
import com.dentallab.domain.payment.model.WorkPaymentStatus;
import com.dentallab.domain.payment.query.WorkBalanceQuery;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.FinalWorkPriceResolver;

@Service
@Transactional(readOnly = true)
public class WorkBalanceProjectionImpl implements WorkBalanceProjection {

    private final FinalWorkPriceResolver priceResolver;
    private final WorkBalanceQuery workBalanceQuery;

    public WorkBalanceProjectionImpl(FinalWorkPriceResolver priceResolver,
                                     WorkBalanceQuery workBalanceQuery) {
        this.priceResolver = priceResolver;
        this.workBalanceQuery = workBalanceQuery;
    }

    @Override
    public WorkBalance project(Long workId) {
        Objects.requireNonNull(workId, "workId must not be null");

        // 1️⃣ Resolve authoritative price (what is owed)
        PriceResolution priceResolution = priceResolver.resolve(
                new PriceResolutionRequest(
                        workId,
                        LocalDate.now(),
                        "DEFAULT"
                )
        );

        BigDecimal totalDue = priceResolution.getFinalPrice();

        // 2️⃣ Resolve how much has already been paid
        BigDecimal totalPaid = workBalanceQuery.getTotalPaidForWork(workId);

        // 3️⃣ Remaining = due - paid
        BigDecimal remaining = totalDue.subtract(totalPaid);

        // 4️⃣ Derive status (never stored, always computed)
        WorkPaymentStatus status;

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            status = WorkPaymentStatus.UNPAID;
        } else if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            status = WorkPaymentStatus.PARTIALLY_PAID;
        } else if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            status = WorkPaymentStatus.PAID;
        } else {
            status = WorkPaymentStatus.OVERPAID;
        }

        return new WorkBalance(
                workId,
                totalDue,
                totalPaid,
                remaining,
                status
        );
    }
}
