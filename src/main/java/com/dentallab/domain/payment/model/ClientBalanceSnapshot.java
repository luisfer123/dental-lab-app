package com.dentallab.domain.payment.model;

import java.math.BigDecimal;
import java.util.Objects;

public class ClientBalanceSnapshot {

    private final Long clientId;
    private final BigDecimal balance;
    private final String currency;
    private final boolean active;

    public ClientBalanceSnapshot(Long clientId,
                                 BigDecimal balance,
                                 String currency,
                                 boolean active) {

        this.clientId = Objects.requireNonNull(clientId);
        this.balance = Objects.requireNonNull(balance);
        this.currency = Objects.requireNonNull(currency);
        this.active = active;
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ClientBalanceSnapshot{" +
                "clientId=" + clientId +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", active=" + active +
                '}';
    }
}
