package com.dentallab.domain.payment.persistence;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.dentallab.domain.payment.model.ClientBalanceSnapshot;
import com.dentallab.domain.payment.query.ClientBalanceQuery;
import com.dentallab.persistence.repository.ClientBalanceRepository;

@Repository
public class JpaClientBalanceQuery implements ClientBalanceQuery {

    private final ClientBalanceRepository balanceRepository;

    public JpaClientBalanceQuery(ClientBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public ClientBalanceSnapshot getClientBalance(Long clientId) {
        Objects.requireNonNull(clientId, "clientId must not be null");

        return balanceRepository.findByClientId(clientId)
                .map(b -> {
                    BigDecimal balance = b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO;

                    // Domain invariant: usable balance is never negative
                    if (balance.signum() < 0) {
                        balance = BigDecimal.ZERO;
                    }

                    return new ClientBalanceSnapshot(
                            clientId,
                            balance,
                            b.getCurrency() != null ? b.getCurrency() : "MXN",
                            Boolean.TRUE.equals(b.getActive())
                    );
                })
                .orElseGet(() ->
                        new ClientBalanceSnapshot(
                                clientId,
                                BigDecimal.ZERO,
                                "MXN",
                                false
                        )
                );
    }
}
