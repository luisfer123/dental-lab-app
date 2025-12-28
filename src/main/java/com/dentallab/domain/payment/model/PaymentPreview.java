package com.dentallab.domain.payment.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PaymentPreview {

    private final Long clientId;
    private final BigDecimal paymentAmount;
    private final BigDecimal totalAllocated;
    private final BigDecimal remainingUnallocated;
    private final List<PaymentAllocationSuggestion> allocations;
    private final ClientBalanceSnapshot clientBalanceAfter;

    public PaymentPreview(Long clientId,
                          BigDecimal paymentAmount,
                          BigDecimal totalAllocated,
                          BigDecimal remainingUnallocated,
                          List<PaymentAllocationSuggestion> allocations,
                          ClientBalanceSnapshot clientBalanceAfter) {

        this.clientId = Objects.requireNonNull(clientId);
        this.paymentAmount = Objects.requireNonNull(paymentAmount);
        this.totalAllocated = Objects.requireNonNull(totalAllocated);
        this.remainingUnallocated = Objects.requireNonNull(remainingUnallocated);
        this.allocations = List.copyOf(allocations);
        this.clientBalanceAfter = clientBalanceAfter; // may be null
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getTotalAllocated() {
        return totalAllocated;
    }

    public BigDecimal getRemainingUnallocated() {
        return remainingUnallocated;
    }

    public List<PaymentAllocationSuggestion> getAllocations() {
        return allocations;
    }

    public ClientBalanceSnapshot getClientBalanceAfter() {
        return clientBalanceAfter;
    }

    @Override
    public String toString() {
        return "PaymentPreview{" +
                "clientId=" + clientId +
                ", paymentAmount=" + paymentAmount +
                ", totalAllocated=" + totalAllocated +
                ", remainingUnallocated=" + remainingUnallocated +
                ", allocations=" + allocations +
                ", clientBalanceAfter=" + clientBalanceAfter +
                '}';
    }
}
