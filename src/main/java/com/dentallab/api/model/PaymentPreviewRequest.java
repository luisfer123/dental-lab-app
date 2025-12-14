package com.dentallab.api.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request model for previewing how a payment could be allocated.
 * No data is persisted when using this model.
 */
public class PaymentPreviewRequest {

    private Long clientId;

    private BigDecimal paymentAmount;

    /** Explicit allocations already chosen by the user */
    private Map<Long, BigDecimal> workAllocations;

    /** Explicit wallet allocation chosen by the user (optional) */
    private BigDecimal walletAmount;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public Map<Long, BigDecimal> getWorkAllocations() { return workAllocations; }
    public void setWorkAllocations(Map<Long, BigDecimal> workAllocations) { this.workAllocations = workAllocations; }

    public BigDecimal getWalletAmount() { return walletAmount; }
    public void setWalletAmount(BigDecimal walletAmount) { this.walletAmount = walletAmount; }
}
