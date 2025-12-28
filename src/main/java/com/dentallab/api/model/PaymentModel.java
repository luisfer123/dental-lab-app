package com.dentallab.api.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

/**
 * HATEOAS model representing a real payment received from a client.
 * A payment may be allocated to one or more works, or partially converted
 * into client balance (wallet).
 */
public class PaymentModel extends RepresentationModel<PaymentModel> {

    private Long id;

    private Long clientId;

    private BigDecimal amountTotal;
    private String currency;

    private String method;
    private String status;

    private String reference;
    private String notes;

    private LocalDateTime receivedAt;
    private LocalDateTime lastUpdated;

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getAmountTotal() { return amountTotal; }
    public void setAmountTotal(BigDecimal amountTotal) { this.amountTotal = amountTotal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public String toString() {
        return "PaymentModel{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", amountTotal=" + amountTotal +
                ", currency='" + currency + '\'' +
                ", method='" + method + '\'' +
                ", status='" + status + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
