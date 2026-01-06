package com.dentallab.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentallab.domain.enums.PaymentStatus;
import com.dentallab.persistence.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

	List<PaymentEntity> findByClientId(Long clientId);

    List<PaymentEntity> findByClientIdAndStatus(Long clientId, PaymentStatus status);

	Collection<PaymentEntity> findByClientIdOrderByReceivedAtDesc(Long clientId);
	
	Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

}
