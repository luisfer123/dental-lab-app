package com.dentallab.service;

import com.dentallab.persistence.entity.WorkerEntity;
import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing lab workers (administrators and technicians).
 */
public interface WorkerService {

    List<WorkerEntity> findAll();

    List<WorkerEntity> findActive();

    Optional<WorkerEntity> findById(Long id);

    WorkerEntity save(WorkerEntity worker);

    void delete(Long id);

    List<WorkerEntity> findAllByRole(String roleName);

    Optional<WorkerEntity> findByUserId(Long userId);

//    boolean existsByEmail(String email);
//
//    boolean existsByPhone(String phone);
}
