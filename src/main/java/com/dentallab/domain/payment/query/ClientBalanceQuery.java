package com.dentallab.domain.payment.query;

import com.dentallab.domain.payment.model.ClientBalanceSnapshot;

public interface ClientBalanceQuery {

    /**
     * Returns the current client balance snapshot.
     * If the client has no balance record, implementations
     * may return a zero-balance inactive snapshot.
     */
    ClientBalanceSnapshot getClientBalance(Long clientId);
}
