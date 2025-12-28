package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "work_item_price_override",
    indexes = {
        @Index(
            name = "idx_override_work",
            columnList = "work_price_id"
        )
    }
)
public class WorkItemPriceOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "override_id")
    private Long overrideId;

    @Column(name = "work_price_id", nullable = false)
    private Long workPriceId;

    @Column(name = "adjustment", nullable = false, precision = 12, scale = 2)
    private BigDecimal adjustment;

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ---------------- getters & setters ----------------

    public Long getOverrideId() {
        return overrideId;
    }

    public void setOverrideId(Long overrideId) {
        this.overrideId = overrideId;
    }

    public Long getWorkPriceId() {
        return workPriceId;
    }

    public void setWorkPriceId(Long workPriceId) {
        this.workPriceId = workPriceId;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    // ---------------- equals / hashCode ----------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkItemPriceOverrideEntity that)) return false;
        return overrideId != null && overrideId.equals(that.overrideId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(overrideId);
    }

    @Override
    public String toString() {
        return "WorkItemPriceOverrideEntity{" +
                "overrideId=" + overrideId +
                ", workPriceId=" + workPriceId +
                ", adjustment=" + adjustment +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
