package com.dentallab.service.impl;

import com.dentallab.persistence.entity.WorkerEntity;
import com.dentallab.persistence.repository.WorkerRepository;
import com.dentallab.service.WorkerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of WorkerService.
 * Handles business logic and enforces authorization where appropriate.
 */
@Service
@Transactional
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository repository;

    public WorkerServiceImpl(WorkerRepository repository) {
        this.repository = repository;
    }

    /* ----------------------------
       Basic CRUD operations
       ---------------------------- */

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public List<WorkerEntity> findAll() {
        return repository.findAll();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public List<WorkerEntity> findActive() {
        return repository.findByActiveTrue();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public Optional<WorkerEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public WorkerEntity save(WorkerEntity worker) {
        return repository.save(worker);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<WorkerEntity> findAllByRole(String roleName) {
        return repository.findAllByRole(roleName);
    }

    /* ----------------------------
       Helper methods (no security)
       ---------------------------- */

    @Override
    public Optional<WorkerEntity> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }

//    @Override
//    public boolean existsByEmail(String email) {
//        return repository.findByEmail(email).isPresent();
//    }
//
//    @Override
//    public boolean existsByPhone(String phone) {
//        return repository.findByPhone(phone).isPresent();
//    }
}
