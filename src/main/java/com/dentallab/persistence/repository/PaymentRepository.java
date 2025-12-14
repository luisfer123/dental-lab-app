package com.dentallab.persistence.repository;

import com.dentallab.domain.enums.PaymentStatus;
import com.dentallab.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByClientIdOrderByReceivedAtDesc(Long clientId);

    List<PaymentEntity> findByClientIdAndStatusOrderByReceivedAtDesc(Long clientId, PaymentStatus status);
}
