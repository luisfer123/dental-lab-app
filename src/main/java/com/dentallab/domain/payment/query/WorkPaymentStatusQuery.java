package com.dentallab.domain.payment.query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Domain query interface exposing payment-related status
 * information for works.
 */
public interface WorkPaymentStatusQuery {

    /**
     * Returns cash-paid amounts per work,
     * considering only RECEIVED payments.
     */
    Map<Long, BigDecimal> findCashPaidAmountsByWorkIds(Collection<Long> workIds);

    /**
     * Returns balance-paid amounts per work
     * (APPLY_WORK movements, absolute values).
     */
    Map<Long, BigDecimal> findBalancePaidAmountsByWorkIds(Collection<Long> workIds);
}
