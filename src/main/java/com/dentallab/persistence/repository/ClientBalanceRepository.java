package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.ClientBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientBalanceRepository extends JpaRepository<ClientBalanceEntity, Long> {

    Optional<ClientBalanceEntity> findByClientId(Long clientId);
}
