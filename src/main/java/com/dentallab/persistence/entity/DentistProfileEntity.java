package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a dentist profile linked to a single ClientEntity.
 * Each dentist profile stores clinic information and audit metadata.
 */
@Entity
@Table(name = "dentist_profile")
public class DentistProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Each dentist profile is uniquely linked to one client */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private ClientEntity client;

    @Column(name = "clinic_name", length = 255)
    private String clinicName;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /* -----------------------
       Lifecycle Callbacks
       ----------------------- */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /* -----------------------
       Getters / Setters (single-line)
       ----------------------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ClientEntity getClient() { return client; }
    public void setClient(ClientEntity client) { this.client = client; }
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /* -----------------------
       equals / hashCode / toString
       ----------------------- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DentistProfileEntity)) return false;
        DentistProfileEntity that = (DentistProfileEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DentistProfileEntity{" +
                "id=" + id +
                ", clientId=" + (client != null ? client.getId() : null) +
                ", clinicName='" + clinicName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}