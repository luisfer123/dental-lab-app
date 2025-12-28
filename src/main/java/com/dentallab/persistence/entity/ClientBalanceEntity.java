package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "client_balance",
    uniqueConstraints = @UniqueConstraint(name = "uq_client_balance_client", columnNames = "client_id")
)
public class ClientBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.currency == null) this.currency = "MXN";
        if (this.active == null) this.active = Boolean.TRUE;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // -------- getters / setters --------

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // -------- equals / hashCode --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientBalanceEntity that)) return false;
        return balanceId != null && balanceId.equals(that.balanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(balanceId);
    }

    @Override
    public String toString() {
        return "ClientBalanceEntity{" +
                "balanceId=" + balanceId +
                ", clientId=" + clientId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", active=" + active +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
