package com.dentallab.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.DentistProfileAssembler;
import com.dentallab.api.model.DentistProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.DentistProfileEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.DentistProfileRepository;
import com.dentallab.service.DentistProfileService;

@Service
public class DentistProfileServiceImpl implements DentistProfileService {

    private static final Logger log = LoggerFactory.getLogger(DentistProfileServiceImpl.class);

    private final DentistProfileRepository dentistRepo;
    private final ClientRepository clientRepo;
    private final DentistProfileAssembler assembler;

    public DentistProfileServiceImpl(DentistProfileRepository dentistRepo,
                                     ClientRepository clientRepo,
                                     DentistProfileAssembler assembler) {
        this.dentistRepo = dentistRepo;
        this.clientRepo = clientRepo;
        this.assembler = assembler;
    }

    /* -------------------- READ ALL -------------------- */
    @Override
    @Transactional(readOnly = true)
    public CollectionModel<DentistProfileModel> getAll() {
        log.debug("Fetching all dentist profiles from database...");
        List<DentistProfileModel> models = dentistRepo.findAll().stream()
                .map(assembler::toModel)
                .toList();
        log.info("Retrieved {} dentist profiles", models.size());
        return CollectionModel.of(models);
    }

    /* -------------------- READ ONE -------------------- */
    @Override
    @Transactional(readOnly = true)
    public DentistProfileModel getById(Long id) {
        log.debug("Fetching dentist profile with id={}", id);
        DentistProfileEntity entity = dentistRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Dentist profile not found with id={}", id);
                    return new RuntimeException("Dentist profile not found with id " + id);
                });
        log.info("Dentist profile found for client id={}", entity.getClient().getId());
        return assembler.toModel(entity);
    }

    /* -------------------- CREATE -------------------- */
    @Override
    @Transactional
    public DentistProfileModel create(DentistProfileModel model) {
        log.info("Creating new dentist profile for clientId={}", model.getClientId());

        if (model.getClientId() == null) {
            log.warn("Failed to create dentist profile: clientId is null");
            throw new IllegalArgumentException("clientId is required");
        }

        ClientEntity client = clientRepo.findById(model.getClientId())
                .orElseThrow(() -> {
                    log.warn("Client not found with id={} while creating dentist profile", model.getClientId());
                    return new RuntimeException("Client not found with id " + model.getClientId());
                });

        DentistProfileEntity entity = assembler.toEntity(model, client);
        DentistProfileEntity saved = dentistRepo.save(entity);

        log.info("Dentist profile created successfully with id={} for clientId={}",
                saved.getId(), client.getId());
        return assembler.toModel(saved);
    }

    /* -------------------- UPDATE -------------------- */
    @Override
    @Transactional
    public DentistProfileModel update(Long id, DentistProfileModel model) {
        log.debug("Updating dentist profile with id={}", id);
        DentistProfileEntity existing = dentistRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update: dentist profile not found with id={}", id);
                    return new RuntimeException("Dentist profile not found with id " + id);
                });

        assembler.updateEntityFromModel(model, existing);
        DentistProfileEntity saved = dentistRepo.save(existing);
        log.info("Dentist profile updated successfully with id={}", id);
        return assembler.toModel(saved);
    }

    /* -------------------- DELETE -------------------- */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Attempting to delete dentist profile with id={}", id);
        if (!dentistRepo.existsById(id)) {
            log.warn("Cannot delete: dentist profile not found with id={}", id);
            throw new RuntimeException("Dentist profile not found with id " + id);
        }
        dentistRepo.deleteById(id);
        log.info("Dentist profile deleted successfully with id={}", id);
    }
}
