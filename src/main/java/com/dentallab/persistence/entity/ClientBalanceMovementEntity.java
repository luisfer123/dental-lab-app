package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "client_balance_movement",
    indexes = {
        @Index(name = "idx_cbm_client", columnList = "client_id, created_at"),
        @Index(name = "idx_cbm_payment", columnList = "payment_id"),
        @Index(name = "idx_cbm_work", columnList = "work_id")
    }
)
public class ClientBalanceMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "amount_change", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountChange;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "work_id")
    private Long workId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "note", length = 255)
    private String note;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.currency == null) this.currency = "MXN";
    }

    // -------- getters / setters --------

    public Long getMovementId() {
        return movementId;
    }

    public void setMovementId(Long movementId) {
        this.movementId = movementId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmountChange() {
        return amountChange;
    }

    public void setAmountChange(BigDecimal amountChange) {
        this.amountChange = amountChange;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // -------- equals / hashCode --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientBalanceMovementEntity that)) return false;
        return movementId != null && movementId.equals(that.movementId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(movementId);
    }

    @Override
    public String toString() {
        return "ClientBalanceMovementEntity{" +
                "movementId=" + movementId +
                ", clientId=" + clientId +
                ", amountChange=" + amountChange +
                ", currency='" + currency + '\'' +
                ", type='" + type + '\'' +
                ", paymentId=" + paymentId +
                ", workId=" + workId +
                ", createdAt=" + createdAt +
                '}';
    }
}
