package com.dentallab.persistence.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dentallab.persistence.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // -------------------------------------------
    // FIND BY NAME
    // -------------------------------------------
    Optional<RoleEntity> findByName(String name);

    // -------------------------------------------
    // ASSIGN ROLE TO USER
    // -------------------------------------------
    @Modifying
    @Query(value = """
        INSERT INTO user_role (user_id, role_id)
        VALUES (:userId, (SELECT role_id FROM role WHERE name = :roleName))
        """, nativeQuery = true)
    void assignRole(@Param("userId") Long userId,
                    @Param("roleName") String roleName);

    // -------------------------------------------
    // REMOVE ROLE FROM USER (optional)
    // -------------------------------------------
    @Modifying
    @Query(value = """
        DELETE ur FROM user_role ur
        JOIN role r ON ur.role_id = r.role_id
        WHERE ur.user_id = :userId
          AND r.name = :roleName
        """, nativeQuery = true)
    void removeRole(@Param("userId") Long userId,
                    @Param("roleName") String roleName);

    // -------------------------------------------
    // GET ROLES FOR A USER (optional)
    // -------------------------------------------
    @Query("""
        SELECT r
        FROM RoleEntity r
        JOIN UserRoleEntity ur ON ur.role.id = r.id
        WHERE ur.user.id = :userId
        """)
    Set<RoleEntity> findRolesByUserId(@Param("userId") Long userId);
}
