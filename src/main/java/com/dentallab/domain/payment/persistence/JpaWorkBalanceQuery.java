package com.dentallab.domain.payment.persistence;

import com.dentallab.domain.payment.query.WorkBalanceQuery;
import com.dentallab.persistence.repository.PaymentAllocationRepository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Objects;

@Repository
public class JpaWorkBalanceQuery implements WorkBalanceQuery {

    private final PaymentAllocationRepository allocationRepository;

    public JpaWorkBalanceQuery(PaymentAllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    @Override
    public BigDecimal getTotalPaidForWork(Long workId) {
        Objects.requireNonNull(workId, "workId must not be null");

        BigDecimal sum = allocationRepository.sumAppliedAmountByWorkId(workId);

        return sum != null ? sum : BigDecimal.ZERO;
    }
}
