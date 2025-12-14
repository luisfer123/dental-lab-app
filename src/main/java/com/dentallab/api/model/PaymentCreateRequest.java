package com.dentallab.api.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request model for registering a payment and allocating it to works.
 * allocations: workId -> amount to apply
 */
public class PaymentCreateRequest {

    private Long clientId;

    private BigDecimal amountTotal;

    private String currency;

    private String method;

    private String reference;

    private String notes;

    private Map<Long, BigDecimal> allocations;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getAmountTotal() { return amountTotal; }
    public void setAmountTotal(BigDecimal amountTotal) { this.amountTotal = amountTotal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Map<Long, BigDecimal> getAllocations() { return allocations; }
    public void setAllocations(Map<Long, BigDecimal> allocations) { this.allocations = allocations; }
}
