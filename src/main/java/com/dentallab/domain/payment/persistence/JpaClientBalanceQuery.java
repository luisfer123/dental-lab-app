package com.dentallab.domain.payment.persistence;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.dentallab.domain.payment.query.ClientBalanceQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class JpaClientBalanceQuery implements ClientBalanceQuery {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal getLedgerBalance(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId must not be null");
        }

        return em.createQuery("""
            select coalesce(sum(m.amountChange), 0)
            from ClientBalanceMovementEntity m
            where m.clientId = :clientId
        """, BigDecimal.class)
        .setParameter("clientId", clientId)
        .getSingleResult();
    }
}
