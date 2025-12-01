package com.dentallab.persistence.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "client")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "second_name", length = 255)
    private String secondName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(name = "second_last_name", length = 255)
    private String secondLastName;

    @Column(name = "primary_email", length = 255)
    private String primaryEmail;

    @Column(name = "primary_phone", length = 50)
    private String primaryPhone;

    @Column(name = "primary_address", length = 255)
    private String primaryAddress;

    @Column(name = "is_active")
    private Boolean active = Boolean.TRUE;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TechnicianProfileEntity technicianProfile;
    
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StudentProfileEntity studentProfile;
    
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DentistProfileEntity dentistProfile;

    /* -----------------------
       Lifecycle callbacks
       ----------------------- */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (active == null) active = Boolean.TRUE;
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
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }
    
    public String getPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
    
    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }
    
    public String getPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(String primaryAddress) { this.primaryAddress = primaryAddress; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public TechnicianProfileEntity getTechnicianProfile() { return technicianProfile; }
    public void setTechnicianProfile(TechnicianProfileEntity technicianProfile) { this.technicianProfile = technicianProfile; }
    
    public StudentProfileEntity getStudentProfile() {return studentProfile; }
    public void setStudentProfile(StudentProfileEntity studentProfile) { this.studentProfile = studentProfile; }
    
    public DentistProfileEntity getDentistProfile() { return dentistProfile; }
    public void setDentistProfile(DentistProfileEntity dentistProfile) { this.dentistProfile = dentistProfile; }

    /* -----------------------
       equals / hashCode / toString
       ----------------------- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientEntity)) return false;
        ClientEntity that = (ClientEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ClientEntity{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", secondLastName='" + secondLastName + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", primaryPhone='" + primaryPhone + '\'' +
                ", primaryAddress='" + primaryAddress + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}