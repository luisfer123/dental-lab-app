package com.dentallab.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dentallab.persistence.entity.PaymentAllocationEntity;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocationEntity, Long> {

	List<PaymentAllocationEntity> findByPaymentId(Long paymentId);

    List<PaymentAllocationEntity> findByWorkId(Long workId);

    @Query("""
        select coalesce(sum(pa.amountApplied), 0)
        from PaymentAllocationEntity pa
        where pa.workId = :workId
    """)
    BigDecimal sumAppliedAmountByWorkId(Long workId);

    @Query("""
        select pa.workId, coalesce(sum(pa.amountApplied), 0)
        from PaymentAllocationEntity pa
        where pa.workId in :workIds
        group by pa.workId
    """)
    List<Object[]> sumAppliedAmountsByWorkIds(List<Long> workIds);
}
