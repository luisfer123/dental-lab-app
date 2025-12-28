package com.dentallab.domain.payment.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PaymentAllocationSuggestion {

    private final Long workId;
    private final BigDecimal suggestedAmount;
    private final WorkPaymentStatus resultingStatus;

    public PaymentAllocationSuggestion(Long workId,
                                       BigDecimal suggestedAmount,
                                       WorkPaymentStatus resultingStatus) {

        this.workId = Objects.requireNonNull(workId);
        this.suggestedAmount = Objects.requireNonNull(suggestedAmount);
        this.resultingStatus = Objects.requireNonNull(resultingStatus);
    }

    public Long getWorkId() {
        return workId;
    }

    public BigDecimal getSuggestedAmount() {
        return suggestedAmount;
    }

    public WorkPaymentStatus getResultingStatus() {
        return resultingStatus;
    }

    @Override
    public String toString() {
        return "PaymentAllocationSuggestion{" +
                "workId=" + workId +
                ", suggestedAmount=" + suggestedAmount +
                ", resultingStatus=" + resultingStatus +
                '}';
    }
}
