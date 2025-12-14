package com.dentallab.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.hateoas.RepresentationModel;

/**
 * Historical record explaining why a client's balance changed.
 * Positive values add balance, negative values consume it.
 */
public class ClientBalanceMovementModel extends RepresentationModel<ClientBalanceMovementModel> {

    private Long id;

    private Long clientId;

    private BigDecimal amountChange;
    private String currency;

    private String type;

    private Long paymentId;
    private Long workId;

    private LocalDateTime createdAt;

    private String note;

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getAmountChange() { return amountChange; }
    public void setAmountChange(BigDecimal amountChange) { this.amountChange = amountChange; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "ClientBalanceMovementModel{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", amountChange=" + amountChange +
                ", type='" + type + '\'' +
                '}';
    }
}
