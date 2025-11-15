package com.dentallab.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.UserAccountEntity;

/**
 * Repository interface for managing {@link UserAccountEntity} persistence.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and query methods
 * for the UserAccountEntity table.
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    /**
     * Finds a user by its unique username.
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the user if found, or empty if not
     */
	@EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserAccountEntity> findByUsername(String username);
	
	@EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
	Optional<UserAccountEntity> findByUsernameOrEmail(String username, String email);
	
	// Find only enabled users by username
	@EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
	Optional<UserAccountEntity> findByUsernameAndEnabledTrue(String username);
	
	// Find only enabled users by username or email
	@EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
	Optional<UserAccountEntity> findByUsernameOrEmailAndEnabledTrue(String username, String email);
}