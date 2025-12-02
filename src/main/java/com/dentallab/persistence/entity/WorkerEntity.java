package com.dentallab.persistence.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Represents a laboratory worker (technician, delivery person, etc.).
 * Linked optionally to a user_account for login.
 */
@Entity
@Table(name = "worker")
public class WorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worker_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserAccountEntity user;

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

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;
    
    public static WorkerEntity create(
            UserAccountEntity user,
            String firstName,
            String lastName,
            String email
    ) {
        WorkerEntity w = new WorkerEntity();
        w.setUser(user);
        w.setFirstName(firstName);
        w.setLastName(lastName);
        w.setEmail(email);
        w.setDisplayName(firstName + " " + lastName);
        w.setActive(true);
        return w;
    }


    // =========================================================
    // Getters & Setters (single line each)
    // =========================================================
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public UserAccountEntity getUser() { return user; } public void setUser(UserAccountEntity user) { this.user = user; }
    public String getDisplayName() { return displayName; } public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getFirstName() { return firstName; } public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; } public void setSecondName(String secondName) { this.secondName = secondName; }
    public String getLastName() { return lastName; } public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSecondLastName() { return secondLastName; } public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Boolean getActive() { return active; } public void setActive(Boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // =========================================================
    // equals / hashCode / toString
    // =========================================================
    @Override
    public boolean equals(Object o) { 
		if (this == o) return true; 
		if (!(o instanceof WorkerEntity)) return false; 
		WorkerEntity that = (WorkerEntity) o; 
		return Objects.equals(id, that.id); 
	}
    
    @Override
    public int hashCode() { 
    	return Objects.hash(id); 
    }
    
    @Override
    public String toString() {
        return "WorkerEntity{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
