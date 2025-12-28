package com.dentallab.persistence.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dentallab.persistence.entity.ClientBalanceMovementEntity;

public interface ClientBalanceMovementRepository extends JpaRepository<ClientBalanceMovementEntity, Long> {

	List<ClientBalanceMovementEntity> findByClientIdOrderByCreatedAtAsc(Long clientId);

	@Query("""
			    select coalesce(sum(m.amountChange), 0)
			    from ClientBalanceMovementEntity m
			    where m.clientId = :clientId
			""")
	BigDecimal sumBalanceByClientId(Long clientId);

	List<ClientBalanceMovementEntity> findByPaymentId(Long paymentId);

	List<ClientBalanceMovementEntity> findByWorkId(Long workId);
}
