package com.dentallab.persistence.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "role",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_name", columnNames = {"name"})
    }
)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    protected RoleEntity() { }

    public RoleEntity(String name) {
        this.name = name;
    }

    public void addUser(UserAccountEntity user) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(this);
        user.getUserRoles().add(userRole);
        this.userRoles.add(userRole);
    }

    public void removeUser(UserAccountEntity user) {
        user.getUserRoles().removeIf(ur -> ur.getRole().equals(this));
        this.userRoles.removeIf(ur -> ur.getUser().equals(user));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<UserRoleEntity> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRoleEntity> userRoles) { this.userRoles = userRoles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;
        RoleEntity that = (RoleEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RoleEntity{id=" + id + ", name='" + name + "'}";
    }
}
