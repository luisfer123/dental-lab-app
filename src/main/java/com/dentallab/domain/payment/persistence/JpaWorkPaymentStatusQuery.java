package com.dentallab.domain.payment.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dentallab.domain.payment.query.WorkPaymentStatusQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * JPA-backed implementation of {@link WorkPaymentStatusQuery}.
 */
@Component
public class JpaWorkPaymentStatusQuery implements WorkPaymentStatusQuery {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Map<Long, BigDecimal> findCashPaidAmountsByWorkIds(Collection<Long> workIds) {
        if (workIds == null || workIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery("""
            select pa.work.id, coalesce(sum(pa.amountApplied), 0)
            from PaymentAllocationEntity pa
            join pa.payment p
            where pa.work.id in :workIds
              and p.status = 'RECEIVED'
            group by pa.work.id
        """, Object[].class)
        .setParameter("workIds", workIds)
        .getResultList();

        return toMap(rows);
    }

    @Override
    public Map<Long, BigDecimal> findBalancePaidAmountsByWorkIds(Collection<Long> workIds) {
        if (workIds == null || workIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery("""
            select m.work.id, coalesce(sum(abs(m.amountChange)), 0)
            from ClientBalanceMovementEntity m
            where m.work.id in :workIds
              and m.type = 'APPLY_WORK'
            group by m.work.id
        """, Object[].class)
        .setParameter("workIds", workIds)
        .getResultList();

        return toMap(rows);
    }

    private Map<Long, BigDecimal> toMap(List<Object[]> rows) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (BigDecimal) row[1]);
        }
        return map;
    }
}
