package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.dentallab.domain.enums.PaymentStatus;

@Entity
@Table(name = "payment")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "method", length = 40)
    private String method;

    @Column(name = "amount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTotal;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status = PaymentStatus.RECEIVED;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.receivedAt == null) this.receivedAt = now;
        if (this.lastUpdated == null) this.lastUpdated = now;
        if (this.currency == null) this.currency = "MXN";
        if (this.status == null) this.status = PaymentStatus.RECEIVED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = Instant.now();
    }

    // -------- getters / setters --------

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(BigDecimal amountTotal) {
        this.amountTotal = amountTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // -------- equals / hashCode (ID-based) --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentEntity that)) return false;
        return paymentId != null && paymentId.equals(that.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(paymentId);
    }

    @Override
    public String toString() {
        return "PaymentEntity{" +
                "paymentId=" + paymentId +
                ", clientId=" + clientId +
                ", receivedAt=" + receivedAt +
                ", method='" + method + '\'' +
                ", amountTotal=" + amountTotal +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                ", reference='" + reference + '\'' +
                '}';
    }
}
