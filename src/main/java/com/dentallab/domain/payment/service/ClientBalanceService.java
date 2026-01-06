package com.dentallab.domain.payment.service;

import java.math.BigDecimal;

/**
 * <p>
 * Service responsible for maintaining and mutating the client "balance" ledger.
 * </p>
 *
 * <p>
 * The authoritative source of truth is {@code client_balance_movement} (ledger).
 * The table {@code client_balance.amount} is a cache of:
 * </p>
 *
 * <pre>
 *   SUM(client_balance_movement.amount_change) for the client
 * </pre>
 *
 * <h3>Concurrency requirement</h3>
 * <p>
 * All operations that mutate balance must be protected by a client-level lock
 * to prevent race conditions (double-spends / lost updates).
 * </p>
 */
public interface ClientBalanceService {

    /**
     *  <p>Returns the current cached balance amount for the client. </p>
     * 
     *  <p>Note: This returns the cached balance, not a recomputed ledger sum. </p>
     *
     * <p>
     * If no {@code client_balance} row exists, implementations may choose to create it
     * lazily as zero.
     * </p>
     */
    BigDecimal getCurrentBalance(Long clientId);
    
    /**
     * <p> Computes the ledger sum of balance for the given client </p>
     * 
     * <p>
     * If no {@code client_balance} row exists, implementations may choose to create it
     * lazily as zero.
     * </p>
     *
     * @param clientId
     * @return
     */
    BigDecimal getLedgerBalance(Long clientId);

    /**
     * Credits client balance (positive movement), typically for payment excess.
     *
     * <p>
     * Creates a {@code client_balance_movement} row with positive {@code amount_change}
     * and updates the {@code client_balance.amount} cache.
     * </p>
     */
    void creditBalance(Long clientId,
                       BigDecimal amount,
                       String type,
                       Long paymentId,
                       Long workId,
                       String note);

    /**
     * Applies client balance to pay a work (negative movement).
     *
     * <p>
     * Creates a {@code client_balance_movement} row with negative {@code amount_change}
     * (type = APPLY_WORK) and updates the {@code client_balance.amount} cache.
     * </p>
     *
     * <p>
     * The service must validate that the client has enough balance to cover {@code amount}.
     * </p>
     */
    void applyBalanceToWork(Long clientId,
                            Long workId,
                            BigDecimal amount,
                            Long paymentId,
                            String note);

    /**
     * Recomputes the cached balance amount from the ledger for the given client
     * and updates {@code client_balance.amount}.
     *
     * <p>
     * Useful for admin repairs, migrations, or integrity checks.
     * </p>
     */
    void recomputeBalanceCache(Long clientId);
}
