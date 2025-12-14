package com.dentallab.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.hateoas.RepresentationModel;

/**
 * Snapshot model of the client's current wallet (saldo favorable).
 * This is a cached value; the source of truth is the balance movements.
 */
public class ClientBalanceModel extends RepresentationModel<ClientBalanceModel> {

    private Long id;

    private Long clientId;

    private BigDecimal amount;
    private String currency;

    private boolean active;

    private LocalDateTime updatedAt;

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "ClientBalanceModel{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", amount=" + amount +
                ", active=" + active +
                '}';
    }
}
