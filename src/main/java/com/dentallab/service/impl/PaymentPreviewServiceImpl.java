package com.dentallab.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.model.PaymentPreviewRequest;
import com.dentallab.api.model.PaymentPreviewResponse;
import com.dentallab.api.model.WorkAllocationSuggestion;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.WorkRepository;
import com.dentallab.service.PaymentPreviewService;

@Service
@Transactional(readOnly = true)
public class PaymentPreviewServiceImpl implements PaymentPreviewService {

    private final WorkRepository workRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    public PaymentPreviewServiceImpl(
            WorkRepository workRepository,
            PaymentAllocationRepository paymentAllocationRepository
    ) {
        this.workRepository = workRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
    }

    @Override
    public PaymentPreviewResponse previewPayment(PaymentPreviewRequest request) {

        BigDecimal paymentAmount = request.getPaymentAmount();
        BigDecimal allocatedToWorks = BigDecimal.ZERO;
        BigDecimal allocatedToWallet = request.getWalletAmount() != null
                ? request.getWalletAmount()
                : BigDecimal.ZERO;

        if (request.getWorkAllocations() != null) {
            for (BigDecimal v : request.getWorkAllocations().values()) {
                if (v != null && v.signum() > 0) {
                    allocatedToWorks = allocatedToWorks.add(v);
                }
            }
        }

        BigDecimal totalAllocated = allocatedToWorks.add(allocatedToWallet);
        BigDecimal remaining = paymentAmount.subtract(totalAllocated);

        PaymentPreviewResponse response = new PaymentPreviewResponse();
        response.setPaymentAmount(paymentAmount);
        response.setAllocatedToWorks(allocatedToWorks);
        response.setAllocatedToWallet(allocatedToWallet);
        response.setUnallocatedAmount(remaining);

        if (remaining.signum() <= 0) {
            response.setFullyAllocated(true);
            response.setMessage("Payment is fully allocated.");
            return response;
        }

        // --------------------------------------------------
        // Find unpaid works not already allocated
        // --------------------------------------------------

        List<WorkAllocationSuggestion> suggestions = new ArrayList<>();

        List<WorkEntity> unpaidWorks =
                workRepository.findUnpaidWorksByClientId(request.getClientId());

        for (WorkEntity work : unpaidWorks) {

            if (request.getWorkAllocations() != null &&
                request.getWorkAllocations().containsKey(work.getId())) {
                continue;
            }

            BigDecimal remainingDue = computeRemainingDue(work.getId());

            if (remainingDue.signum() <= 0) continue;

            BigDecimal suggested = remaining.min(remainingDue);

            WorkAllocationSuggestion s = new WorkAllocationSuggestion();
            s.setWorkId(work.getId());
            s.setRemainingDue(remainingDue);
            s.setSuggestedAmount(suggested);

            suggestions.add(s);

            remaining = remaining.subtract(suggested);

            if (remaining.signum() <= 0) break;
        }

        response.setSuggestedAllocations(suggestions);
        response.setFullyAllocated(false);
        response.setMessage("Payment is not fully allocated. Suggestions provided.");

        return response;
    }

    private BigDecimal computeRemainingDue(Long workId) {
        // Placeholder – you already know how to compute this
        // price - SUM(payment_allocation) - SUM(wallet APPLY_WORK)
        return BigDecimal.ZERO;
    }
}
