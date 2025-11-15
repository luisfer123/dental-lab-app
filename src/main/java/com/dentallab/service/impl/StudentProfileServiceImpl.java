package com.dentallab.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.StudentProfileAssembler;
import com.dentallab.api.model.StudentProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.StudentProfileEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.StudentProfileRepository;
import com.dentallab.service.StudentProfileService;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    private static final Logger log = LoggerFactory.getLogger(StudentProfileServiceImpl.class);

    private final StudentProfileRepository studentRepo;
    private final ClientRepository clientRepo;
    private final StudentProfileAssembler assembler;

    public StudentProfileServiceImpl(StudentProfileRepository studentRepo,
                                     ClientRepository clientRepo,
                                     StudentProfileAssembler assembler) {
        this.studentRepo = studentRepo;
        this.clientRepo = clientRepo;
        this.assembler = assembler;
    }

    /* -------------------- READ ALL -------------------- */
    @Override
    @Transactional(readOnly = true)
    public CollectionModel<StudentProfileModel> getAll() {
        log.debug("Fetching all student profiles from database...");
        List<StudentProfileModel> models = studentRepo.findAll().stream()
                .map(assembler::toModel)
                .toList();
        log.info("Retrieved {} student profiles", models.size());
        return CollectionModel.of(models);
    }

    /* -------------------- READ ONE -------------------- */
    @Override
    @Transactional(readOnly = true)
    public StudentProfileModel getById(Long id) {
        log.debug("Fetching student profile with id={}", id);
        StudentProfileEntity entity = studentRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Student profile not found with id={}", id);
                    return new RuntimeException("Student profile not found with id " + id);
                });
        log.info("Student profile found for client id={}", entity.getClient().getId());
        return assembler.toModel(entity);
    }

    /* -------------------- CREATE -------------------- */
    @Override
    @Transactional
    public StudentProfileModel create(StudentProfileModel model) {
        log.info("Creating new student profile for clientId={}", model.getClientId());

        if (model.getClientId() == null) {
            log.warn("Failed to create student profile: clientId is null");
            throw new IllegalArgumentException("clientId is required");
        }

        ClientEntity client = clientRepo.findById(model.getClientId())
                .orElseThrow(() -> {
                    log.warn("Client not found with id={} while creating student profile", model.getClientId());
                    return new RuntimeException("Client not found with id " + model.getClientId());
                });

        StudentProfileEntity entity = assembler.toEntity(model, client);
        StudentProfileEntity saved = studentRepo.save(entity);

        log.info("Student profile created successfully with id={} for clientId={}",
                saved.getId(), client.getId());
        return assembler.toModel(saved);
    }

    /* -------------------- UPDATE -------------------- */
    @Override
    @Transactional
    public StudentProfileModel update(Long id, StudentProfileModel model) {
        log.debug("Updating student profile with id={}", id);
        StudentProfileEntity existing = studentRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update: student profile not found with id={}", id);
                    return new RuntimeException("Student profile not found with id " + id);
                });

        assembler.updateEntityFromModel(model, existing);
        StudentProfileEntity saved = studentRepo.save(existing);
        log.info("Student profile updated successfully with id={}", id);
        return assembler.toModel(saved);
    }

    /* -------------------- DELETE -------------------- */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Attempting to delete student profile with id={}", id);
        if (!studentRepo.existsById(id)) {
            log.warn("Cannot delete: student profile not found with id={}", id);
            throw new RuntimeException("Student profile not found with id " + id);
        }
        studentRepo.deleteById(id);
        log.info("Student profile deleted successfully with id={}", id);
    }
}
