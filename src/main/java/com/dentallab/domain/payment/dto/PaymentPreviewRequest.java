package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request object used to preview how a payment would be allocated
 * across a set of selected works.
 */
public class PaymentPreviewRequest {

    @NotNull
    private Long clientId;

    @NotNull
    @Positive
    private BigDecimal paymentAmount;

    @NotEmpty
    private List<Long> selectedWorkIds;

    /**
     * Optional per-work allocation overrides.
     * Null or empty => automatic allocation (Option A).
     */
    private Map<Long, BigDecimal> allocationOverrides;

    public PaymentPreviewRequest() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public List<Long> getSelectedWorkIds() {
        return selectedWorkIds;
    }

    public void setSelectedWorkIds(List<Long> selectedWorkIds) {
        this.selectedWorkIds = selectedWorkIds;
    }

    public Map<Long, BigDecimal> getAllocationOverrides() {
        return allocationOverrides;
    }

    public void setAllocationOverrides(Map<Long, BigDecimal> allocationOverrides) {
        this.allocationOverrides = allocationOverrides;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentPreviewRequest)) return false;
        PaymentPreviewRequest that = (PaymentPreviewRequest) o;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(paymentAmount, that.paymentAmount)
                && Objects.equals(selectedWorkIds, that.selectedWorkIds)
                && Objects.equals(allocationOverrides, that.allocationOverrides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, paymentAmount, selectedWorkIds, allocationOverrides);
    }

    @Override
    public String toString() {
        return "PaymentPreviewRequest{" +
                "clientId=" + clientId +
                ", paymentAmount=" + paymentAmount +
                ", selectedWorkIds=" + selectedWorkIds +
                ", allocationOverrides=" + allocationOverrides +
                '}';
    }
}
