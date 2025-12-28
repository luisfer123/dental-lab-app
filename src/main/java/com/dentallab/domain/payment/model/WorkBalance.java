package com.dentallab.domain.payment.model;

import java.math.BigDecimal;
import java.util.Objects;

public class WorkBalance {

    private final Long workId;
    private final BigDecimal totalDue;
    private final BigDecimal totalPaid;
    private final BigDecimal remaining;
    private final WorkPaymentStatus status;

    public WorkBalance(Long workId,
                       BigDecimal totalDue,
                       BigDecimal totalPaid,
                       BigDecimal remaining,
                       WorkPaymentStatus status) {

        this.workId = Objects.requireNonNull(workId);
        this.totalDue = Objects.requireNonNull(totalDue);
        this.totalPaid = Objects.requireNonNull(totalPaid);
        this.remaining = Objects.requireNonNull(remaining);
        this.status = Objects.requireNonNull(status);
    }

    public Long getWorkId() {
        return workId;
    }

    public BigDecimal getTotalDue() {
        return totalDue;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public WorkPaymentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "WorkBalance{" +
                "workId=" + workId +
                ", totalDue=" + totalDue +
                ", totalPaid=" + totalPaid +
                ", remaining=" + remaining +
                ", status=" + status +
                '}';
    }
}
