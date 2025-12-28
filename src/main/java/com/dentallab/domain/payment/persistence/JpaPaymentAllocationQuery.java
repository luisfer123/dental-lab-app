package com.dentallab.domain.payment.persistence;

import org.springframework.stereotype.Repository;

import com.dentallab.domain.payment.query.PaymentAllocationQuery;
import com.dentallab.persistence.repository.PaymentAllocationRepository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public class JpaPaymentAllocationQuery implements PaymentAllocationQuery {

    private final PaymentAllocationRepository allocationRepository;

    public JpaPaymentAllocationQuery(PaymentAllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    @Override
    public Map<Long, BigDecimal> getAppliedAmountsByWorkIds(Set<Long> workIds) {
        Objects.requireNonNull(workIds, "workIds must not be null");

        Map<Long, BigDecimal> result = new HashMap<>();

        // Initialize all works with zero
        for (Long workId : workIds) {
            result.put(workId, BigDecimal.ZERO);
        }

        if (workIds.isEmpty()) {
            return result;
        }

        allocationRepository
                .sumAppliedAmountsByWorkIds(new ArrayList<>(workIds))
                .forEach(row -> {
                    Long workId = (Long) row[0];
                    BigDecimal amount = (BigDecimal) row[1];

                    if (workId != null && amount != null) {
                        result.put(workId, amount);
                    }
                });

        return result;
    }
}
