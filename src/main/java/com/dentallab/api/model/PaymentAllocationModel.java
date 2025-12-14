package com.dentallab.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.hateoas.RepresentationModel;

/**
 * Represents how a payment is applied to a specific work.
 * Supports partial payments and multiple allocations per payment.
 */
public class PaymentAllocationModel extends RepresentationModel<PaymentAllocationModel> {

    private Long id;

    private Long paymentId;
    private Long workId;

    private BigDecimal amountApplied;

    private LocalDateTime createdAt;

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }

    public BigDecimal getAmountApplied() { return amountApplied; }
    public void setAmountApplied(BigDecimal amountApplied) { this.amountApplied = amountApplied; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "PaymentAllocationModel{" +
                "id=" + id +
                ", paymentId=" + paymentId +
                ", workId=" + workId +
                ", amountApplied=" + amountApplied +
                '}';
    }
}
