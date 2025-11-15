package com.dentallab.persistence.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenId(String tokenId);
    
    long deleteByExpiryDateBefore(Instant cutoff);  // for scheduled cleanup
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revoked = true WHERE t.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);
}
