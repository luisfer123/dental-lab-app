package com.dentallab.persistence.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "user_account")
public class UserAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "locked", nullable = false)
    private Boolean locked = false;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    // --- Constructors ---
    public UserAccountEntity() {}

    public UserAccountEntity(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = true;
        this.locked = false;
    }
    
    public void addRole(RoleEntity role) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(this);
        userRole.setRole(role);
        userRoles.add(userRole);
        role.getUserRoles().add(userRole);
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    public void removeRole(RoleEntity role) {
        userRoles.removeIf(ur -> ur.getRole().equals(role));
        role.getUserRoles().removeIf(ur -> ur.getUser().equals(this));
    }
    
    @Transient
    public List<String> getRoleNames() {
        return userRoles.stream()
            .map(ur -> ur.getRole().getName())
            .toList();
    }


    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Boolean isEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean isLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }

    public Instant getCreatedAt() { return createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }    
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<UserRoleEntity> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRoleEntity> userRoles) { this.userRoles = userRoles; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccountEntity)) return false;
        UserAccountEntity that = (UserAccountEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // createdAt should be set by DB, so we don’t add a setter
}