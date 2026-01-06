package com.dentallab.domain.payment.query;

import java.math.BigDecimal;

/**
 * Read-only query API for client balance.
 */
public interface ClientBalanceQuery {

    /**
     * Returns the authoritative ledger balance (SUM of movements).
     */
    BigDecimal getLedgerBalance(Long clientId);
}
