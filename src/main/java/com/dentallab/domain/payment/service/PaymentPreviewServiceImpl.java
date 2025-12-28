package com.dentallab.domain.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.payment.model.ClientBalanceSnapshot;
import com.dentallab.domain.payment.model.PaymentAllocationRequest;
import com.dentallab.domain.payment.model.PaymentAllocationSuggestion;
import com.dentallab.domain.payment.model.PaymentPreview;
import com.dentallab.domain.payment.model.WorkBalance;
import com.dentallab.domain.payment.model.WorkPaymentStatus;
import com.dentallab.domain.payment.query.ClientBalanceQuery;

@Service
@Transactional(readOnly = true)
public class PaymentPreviewServiceImpl implements PaymentPreviewService {

    private final WorkBalanceProjection workBalanceProjection;
    private final ClientBalanceQuery clientBalanceQuery;

    public PaymentPreviewServiceImpl(WorkBalanceProjection workBalanceProjection,
                                     ClientBalanceQuery clientBalanceQuery) {
        this.workBalanceProjection = workBalanceProjection;
        this.clientBalanceQuery = clientBalanceQuery;
    }

    @Override
    public PaymentPreview preview(Long clientId,
                                  BigDecimal paymentAmount,
                                  List<PaymentAllocationRequest> requestedAllocations) {

        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(paymentAmount, "paymentAmount must not be null");
        Objects.requireNonNull(requestedAllocations, "requestedAllocations must not be null");

        if (paymentAmount.signum() <= 0) {
            throw new IllegalArgumentException("paymentAmount must be > 0");
        }

        // 1️⃣ Resolve current balances for each work
        Map<Long, WorkBalance> balancesByWork = new HashMap<>();

        for (PaymentAllocationRequest req : requestedAllocations) {
            WorkBalance balance = workBalanceProjection.project(req.getWorkId());
            balancesByWork.put(req.getWorkId(), balance);
        }

        // 2️⃣ Validate requested allocations against remaining balance
        BigDecimal totalRequested = BigDecimal.ZERO;

        for (PaymentAllocationRequest req : requestedAllocations) {
            WorkBalance balance = balancesByWork.get(req.getWorkId());

            if (req.getAmount().signum() < 0) {
                throw new IllegalArgumentException(
                        "Allocation amount cannot be negative for work " + req.getWorkId()
                );
            }

            if (req.getAmount().compareTo(balance.getRemaining()) > 0) {
                throw new IllegalArgumentException(
                        "Allocation exceeds remaining balance for work " + req.getWorkId()
                );
            }

            totalRequested = totalRequested.add(req.getAmount());
        }

        if (totalRequested.compareTo(paymentAmount) > 0) {
            throw new IllegalArgumentException(
                    "Total allocated amount exceeds payment amount"
            );
        }

        // 3️⃣ Build allocation suggestions with resulting status
        List<PaymentAllocationSuggestion> suggestions = new ArrayList<>();

        for (PaymentAllocationRequest req : requestedAllocations) {
            WorkBalance balance = balancesByWork.get(req.getWorkId());

            BigDecimal newRemaining =
                    balance.getRemaining().subtract(req.getAmount());

            WorkPaymentStatus resultingStatus;

            if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                resultingStatus = WorkPaymentStatus.PAID;
            } else if (newRemaining.compareTo(balance.getTotalDue()) == 0) {
                resultingStatus = WorkPaymentStatus.UNPAID;
            } else {
                resultingStatus = WorkPaymentStatus.PARTIALLY_PAID;
            }

            suggestions.add(
                    new PaymentAllocationSuggestion(
                            req.getWorkId(),
                            req.getAmount(),
                            resultingStatus
                    )
            );
        }

        // 4️⃣ Compute leftover (goes to client balance)
        BigDecimal remainingUnallocated =
                paymentAmount.subtract(totalRequested);

        ClientBalanceSnapshot clientBalanceAfter = null;

        if (remainingUnallocated.signum() > 0) {
            ClientBalanceSnapshot currentBalance =
                    clientBalanceQuery.getClientBalance(clientId);

            BigDecimal newBalance =
                    currentBalance.getBalance().add(remainingUnallocated);

            clientBalanceAfter = new ClientBalanceSnapshot(
                    clientId,
                    newBalance,
                    currentBalance.getCurrency(),
                    currentBalance.isActive()
            );
        }

        return new PaymentPreview(
                clientId,
                paymentAmount,
                totalRequested,
                remainingUnallocated,
                suggestions,
                clientBalanceAfter
        );
    }
}
