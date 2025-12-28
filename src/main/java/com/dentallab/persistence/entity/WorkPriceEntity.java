package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "work_price",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_work_price_work",
        columnNames = "work_id"
    )
)
public class WorkPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private WorkEntity work;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_group", nullable = false, length = 50)
    private String priceGroup = "DEFAULT";

    @Column(name = "currency", length = 3)
    private String currency = "MXN";

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ---------------- getters & setters ----------------

    public Long getPriceId() {
        return priceId;
    }

    public void setPriceId(Long priceId) {
        this.priceId = priceId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public void setWork(WorkEntity work) {
		this.work = work;
	}
    
    public WorkEntity getWork() {
    	return this.work;
    }

    public String getPriceGroup() {
        return priceGroup;
    }

    public void setPriceGroup(String priceGroup) {
        this.priceGroup = priceGroup;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
        if (!(o instanceof WorkPriceEntity that)) return false;
        return priceId != null && priceId.equals(that.priceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(priceId);
    }

    @Override
    public String toString() {
        return "WorkPriceEntity{" +
                "priceId=" + priceId +
                ", workId=" + work.getId() +
                ", price=" + price +
                ", priceGroup='" + priceGroup + '\'' +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
