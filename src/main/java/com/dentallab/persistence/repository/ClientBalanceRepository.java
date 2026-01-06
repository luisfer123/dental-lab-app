package com.dentallab.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dentallab.persistence.entity.ClientBalanceEntity;

import jakarta.persistence.LockModeType;

public interface ClientBalanceRepository extends JpaRepository<ClientBalanceEntity, Long> {

	Optional<ClientBalanceEntity> findByClientId(Long clientId);

    /**
     * Loads the client balance row using a PESSIMISTIC_WRITE lock.
     *
     * <p>
     * This is the key to preventing concurrent updates from producing incorrect balances.
     * </p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select cb
        from ClientBalanceEntity cb
        where cb.clientId = :clientId
    """)
    Optional<ClientBalanceEntity> findByClientIdForUpdate(@Param("clientId") Long clientId);
}
