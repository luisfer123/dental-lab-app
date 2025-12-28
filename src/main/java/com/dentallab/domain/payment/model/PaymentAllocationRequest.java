package com.dentallab.domain.payment.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PaymentAllocationRequest {

    private final Long workId;
    private final BigDecimal amount;

    public PaymentAllocationRequest(Long workId, BigDecimal amount) {
        this.workId = Objects.requireNonNull(workId);
        this.amount = Objects.requireNonNull(amount);
    }

    public Long getWorkId() {
        return workId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "PaymentAllocationRequest{" +
                "workId=" + workId +
                ", amount=" + amount +
                '}';
    }
}
