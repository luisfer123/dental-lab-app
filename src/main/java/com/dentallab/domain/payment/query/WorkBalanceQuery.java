package com.dentallab.domain.payment.query;

import java.math.BigDecimal;

public interface WorkBalanceQuery {

    /**
     * Total amount already applied to the given work
     * via payment allocations.
     *
     * @param workId work identifier
     * @return sum of applied amounts (>= 0)
     */
    BigDecimal getTotalPaidForWork(Long workId);
}
