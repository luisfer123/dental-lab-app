package com.dentallab.persistence.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(
    name = "refresh_token",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_refresh_token_token", columnNames = {"token"}),
        @UniqueConstraint(name = "uk_refresh_token_jti",   columnNames = {"jti"})
    },
    indexes = {
        @Index(name = "idx_refresh_token_user_id",   columnList = "user_id"),
        @Index(name = "idx_refresh_token_expiry",    columnList = "expiry_date"),
        @Index(name = "idx_refresh_token_revoked",   columnList = "revoked")
    }
)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                         // DB primary key (internal only)

    @Column(nullable = false, length = 500)
    private String token;                    // The actual signed refresh JWT

    @Column(name = "jti", unique = true, nullable = false, length = 64)
    private String tokenId;                  // JWT ID claim (jti) — unique per issued token

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private UserAccountEntity user;          // Owner (don’t store primitive userId here)

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;              // UTC expiry

    @Column(nullable = false)
    private boolean revoked = false;         // Revoked (on rotation or logout)

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // --- Getters / Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public UserAccountEntity getUser() { return user; }
    public void setUser(UserAccountEntity user) { this.user = user; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // --- Equality & Hashing ---
    // For JPA entities, prefer identity equality once persisted.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshTokenEntity other)) return false;
        return id != null && id.equals(other.id);
    }
    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "RefreshTokenEntity{id=" + id +
               ", jti=" + tokenId +
               ", user=" + (user != null ? user.getId() : null) +
               ", expires=" + expiryDate +
               ", revoked=" + revoked + "}";
    }
}
