package com.dentallab.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.WorkerEntity;

@Repository
public interface WorkerRepository extends JpaRepository<WorkerEntity, Long> {

    // Find a worker by linked user ID
    Optional<WorkerEntity> findByUser_Id(Long userId);

//    // Search workers by email or phone (useful for validations)
//    Optional<WorkerEntity> findByEmail(String email);
//    Optional<WorkerEntity> findByPhone(String phone);

    // Get all active workers
    List<WorkerEntity> findByActiveTrue();

    // Example of custom JPQL query: get all workers by role name
    @Query("""
            SELECT w
            FROM WorkerEntity w
            JOIN w.user u
            JOIN u.userRoles ur
            JOIN ur.role r
            WHERE r.name = :roleName
        """)
        List<WorkerEntity> findAllByRole(@Param("roleName") String roleName);
    

}
