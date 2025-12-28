package com.dentallab.domain.payment.query;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface PaymentAllocationQuery {

    /**
     * Returns total applied amount per work.
     *
     * @param workIds set of work IDs
     * @return map: workId -> total applied amount (>= 0)
     */
    Map<Long, BigDecimal> getAppliedAmountsByWorkIds(Set<Long> workIds);
}
