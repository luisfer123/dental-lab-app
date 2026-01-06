package com.dentallab.domain.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.payment.service.ClientBalanceService;
import com.dentallab.persistence.entity.ClientBalanceEntity;
import com.dentallab.persistence.entity.ClientBalanceMovementEntity;
import com.dentallab.persistence.repository.ClientBalanceMovementRepository;
import com.dentallab.persistence.repository.ClientBalanceRepository;

/**
 * <p>
 * Default implementation of {@link ClientBalanceService}.
 * </p>
 *
 * <h3>How balance works</h3>
 * <ul>
 *   <li>{@code client_balance_movement} is the ledger (source of truth).</li>
 *   <li>{@code client_balance.amount} is a cache (fast read).</li>
 * </ul>
 *
 * <h3>Concurrency strategy</h3>
 * <p>
 * All mutations acquire a PESSIMISTIC_WRITE lock on the {@code client_balance} row
 * for the client. This prevents concurrent updates from producing incorrect totals.
 * </p>
 */
@Service
@Transactional
public class ClientBalanceServiceImpl implements ClientBalanceService {

    private static final Logger log = LoggerFactory.getLogger(ClientBalanceServiceImpl.class);

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final ClientBalanceRepository clientBalanceRepository;
    private final ClientBalanceMovementRepository movementRepository;

    public ClientBalanceServiceImpl(
            ClientBalanceRepository clientBalanceRepository,
            ClientBalanceMovementRepository movementRepository
    ) {
        this.clientBalanceRepository = clientBalanceRepository;
        this.movementRepository = movementRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long clientId) {
        Objects.requireNonNull(clientId, "clientId must not be null");

        Optional<ClientBalanceEntity> cb = clientBalanceRepository.findByClientId(clientId);

        BigDecimal amount = cb.map(ClientBalanceEntity::getAmount).orElse(ZERO);
        return normalize(amount);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getLedgerBalance(Long clientId) {
        Objects.requireNonNull(clientId, "clientId must not be null");

        BigDecimal sum =
            movementRepository.sumLedgerBalanceByClientId(clientId);

        return normalize(sum == null ? ZERO : sum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void creditBalance(Long clientId,
                              BigDecimal amount,
                              String type,
                              Long paymentId,
                              Long workId,
                              String note) {

        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(type, "type must not be null");

        BigDecimal credit = normalize(amount);
        if (credit.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("credit amount must be > 0");
        }

        log.info("Crediting client balance: clientId={}, amount={}, type={}, paymentId={}, workId={}",
                clientId, credit, type, paymentId, workId);

        ClientBalanceEntity cb = lockOrCreateBalanceRow(clientId);

        assertBalanceActive(cb);

        // 1) Create ledger movement (positive)
        ClientBalanceMovementEntity m = new ClientBalanceMovementEntity();
        m.setClientId(clientId);
        m.setAmountChange(credit);
        m.setType(type);
        m.setPaymentId(paymentId);
        m.setWorkId(workId);
        m.setNote(note);
        m.setCreatedAt(Instant.now());
        movementRepository.save(m);

        // 2) Update cache
        BigDecimal newAmount = normalize(cb.getAmount().add(credit));
        cb.setAmount(newAmount);
        
        cb.setAmount(newAmount);
        clientBalanceRepository.save(cb);

        log.debug("Client balance credited: clientId={}, newBalance={}", clientId, newAmount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyBalanceToWork(Long clientId,
                                  Long workId,
                                  BigDecimal amount,
                                  Long paymentId,
                                  String note) {

        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(workId, "workId must not be null");

        BigDecimal debit = normalize(amount);
        if (debit.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("debit amount must be > 0");
        }

        log.info("Applying client balance to work: clientId={}, workId={}, amount={}, paymentId={}",
                clientId, workId, debit, paymentId);

        ClientBalanceEntity cb = lockOrCreateBalanceRow(clientId);

        assertBalanceActive(cb);

        BigDecimal current = normalize(cb.getAmount());
        if (current.compareTo(debit) < 0) {
            log.warn("Insufficient client balance: clientId={}, currentBalance={}, requestedDebit={}",
                    clientId, current, debit);
            throw new IllegalStateException("Insufficient client balance");
        }

        // 1) Create ledger movement (negative)
        ClientBalanceMovementEntity m = new ClientBalanceMovementEntity();
        m.setClientId(clientId);
        m.setAmountChange(debit.negate()); // APPLY_WORK must be negative
        m.setType("APPLY_WORK");
        m.setPaymentId(paymentId);
        m.setWorkId(workId);
        m.setNote(note);
        m.setCreatedAt(Instant.now());
        movementRepository.save(m);

        // 2) Update cache
        BigDecimal newAmount = normalize(current.subtract(debit));
        cb.setAmount(newAmount);
        clientBalanceRepository.save(cb);

        log.debug("Client balance applied: clientId={}, workId={}, newBalance={}",
                clientId, workId, newAmount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recomputeBalanceCache(Long clientId) {
        Objects.requireNonNull(clientId, "clientId must not be null");

        log.info("Recomputing client balance cache from ledger: clientId={}", clientId);

        ClientBalanceEntity cb = lockOrCreateBalanceRow(clientId);

        BigDecimal rawSum = movementRepository.sumLedgerBalanceByClientId(clientId);
        BigDecimal ledgerSum = normalize(rawSum == null ? ZERO : rawSum);

        if (ledgerSum.compareTo(ZERO) < 0) {
            // By your schema, client_balance.amount has CHECK(amount >= 0).
            // A negative sum would mean corruption or inconsistent movement usage.
            log.error("Ledger sum is negative; cannot update cache: clientId={}, ledgerSum={}",
                    clientId, ledgerSum);
            throw new IllegalStateException("Ledger sum is negative; balance cache cannot be updated");
        }

        BigDecimal old = normalize(cb.getAmount());
        cb.setAmount(ledgerSum);
        clientBalanceRepository.save(cb);

        log.debug("Balance cache recomputed: clientId={}, oldBalance={}, newBalance={}",
                clientId, old, ledgerSum);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    /**
     * Loads the {@code client_balance} row using a pessimistic lock,
     * or creates it if missing (lazy creation).
     *
     * <p>
     * This ensures all balance mutations serialize for a given client.
     * </p>
     */
    private ClientBalanceEntity lockOrCreateBalanceRow(Long clientId) {

        Optional<ClientBalanceEntity> existing =
                clientBalanceRepository.findByClientIdForUpdate(clientId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // If no row exists yet, create one at zero.
        // This is safe because we are inside a transaction.
        ClientBalanceEntity cb = new ClientBalanceEntity();
        cb.setClientId(clientId);
        cb.setAmount(ZERO);
        cb.setCurrency("MXN");
        cb.setActive(true);

        // TODO: if your ClientBalanceEntity has a relation to ClientEntity instead of clientId:
        //       setClient(clientRepository.getReferenceById(clientId));

        try {
            ClientBalanceEntity saved = clientBalanceRepository.save(cb);
            log.debug("Created new client_balance row: clientId={}, balanceId={}",
                    clientId, saved.getBalanceId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            // Another transaction created it first
            return clientBalanceRepository.findByClientIdForUpdate(clientId)
                    .orElseThrow();
        }
    }

    /**
     * Ensures the client is allowed to use the balance feature.
     */
    private static void assertBalanceActive(ClientBalanceEntity cb) {
        if (cb.getActive() == null || !cb.getActive()) {
            throw new IllegalStateException("Client balance is not active for this client");
        }
    }

    /**
     * Canonical money normalization: always scale(2).
     */
    private static BigDecimal normalize(BigDecimal v) {
        return v == null ? ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}
