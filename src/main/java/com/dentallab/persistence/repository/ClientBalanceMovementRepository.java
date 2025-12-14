package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.ClientBalanceMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ClientBalanceMovementRepository extends JpaRepository<ClientBalanceMovementEntity, Long> {

    List<ClientBalanceMovementEntity> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ClientBalanceMovementEntity> findByPaymentId(Long paymentId);

    List<ClientBalanceMovementEntity> findByWorkId(Long workId);

    @Query("select coalesce(sum(m.amountChange), 0) from ClientBalanceMovementEntity m where m.clientId = :clientId")
    BigDecimal sumAmountChangeByClientId(@Param("clientId") Long clientId);

    @Query("select coalesce(sum(abs(m.amountChange)), 0) from ClientBalanceMovementEntity m where m.workId = :workId and m.type = :type")
    BigDecimal sumAbsAmountChangeByWorkIdAndType(@Param("workId") Long workId, @Param("type") String type);
}
