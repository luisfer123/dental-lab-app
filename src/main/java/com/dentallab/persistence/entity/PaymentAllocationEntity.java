package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "payment_allocation",
    indexes = {
        @Index(name = "idx_pa_work", columnList = "work_id")
    }
)
public class PaymentAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "amount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountApplied;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    // -------- getters / setters --------

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }

    public BigDecimal getAmountApplied() {
        return amountApplied;
    }

    public void setAmountApplied(BigDecimal amountApplied) {
        this.amountApplied = amountApplied;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // -------- equals / hashCode --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentAllocationEntity that)) return false;
        return allocationId != null && allocationId.equals(that.allocationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(allocationId);
    }

    @Override
    public String toString() {
        return "PaymentAllocationEntity{" +
                "allocationId=" + allocationId +
                ", paymentId=" + paymentId +
                ", workId=" + workId +
                ", amountApplied=" + amountApplied +
                ", createdAt=" + createdAt +
                '}';
    }
}
