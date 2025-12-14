package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.PaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocationEntity, Long> {

    List<PaymentAllocationEntity> findByPaymentId(Long paymentId);

    List<PaymentAllocationEntity> findByWorkId(Long workId);

    @Query("select coalesce(sum(a.amountApplied), 0) from PaymentAllocationEntity a where a.paymentId = :paymentId")
    BigDecimal sumAppliedByPaymentId(@Param("paymentId") Long paymentId);

    @Query("select coalesce(sum(a.amountApplied), 0) from PaymentAllocationEntity a where a.workId = :workId")
    BigDecimal sumAppliedByWorkId(@Param("workId") Long workId);
}
