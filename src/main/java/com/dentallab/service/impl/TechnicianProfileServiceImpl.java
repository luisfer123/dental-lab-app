package com.dentallab.service.impl;

import com.dentallab.api.assembler.TechnicianProfileAssembler;
import com.dentallab.api.model.TechnicianProfileModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.TechnicianProfileEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.TechnicianProfileRepository;
import com.dentallab.service.TechnicianProfileService;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class TechnicianProfileServiceImpl implements TechnicianProfileService {

    private static final Logger log = LoggerFactory.getLogger(TechnicianProfileServiceImpl.class);

    private final TechnicianProfileRepository techRepo;
    private final ClientRepository clientRepo;
    private final TechnicianProfileAssembler assembler;

    public TechnicianProfileServiceImpl(
            TechnicianProfileRepository techRepo,
            ClientRepository clientRepo,
            TechnicianProfileAssembler assembler) {
        this.techRepo = techRepo;
        this.clientRepo = clientRepo;
        this.assembler = assembler;
    }

    private Sort parseSort(String[] sortParams) {
        if (sortParams == null || sortParams.length == 0)
            return Sort.unsorted();
        String[] parts = sortParams[0].split(",");
        String field = parts[0];
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
    }

    @Override
    public Page<TechnicianProfileModel> getAll(int page, int size, String[] sortParams) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sortParams));
        Page<TechnicianProfileEntity> entities = techRepo.findAll(pageable);
        return entities.map(assembler::toModel);
    }

    @Override
    public TechnicianProfileModel getById(Long id) {
        TechnicianProfileEntity entity = techRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Technician profile not found: " + id));
        return assembler.toModel(entity);
    }

    @Override
    public TechnicianProfileModel create(TechnicianProfileModel model) {
        if (model.getClientId() == null)
            throw new IllegalArgumentException("Client ID is required to create a TechnicianProfile.");

        ClientEntity client = clientRepo.findById(model.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + model.getClientId()));

        TechnicianProfileEntity entity = assembler.toEntity(model, client);
        techRepo.save(entity);
        log.info("Created technician profile for client {}", model.getClientId());
        return assembler.toModel(entity);
    }

    @Override
    public TechnicianProfileModel update(Long id, TechnicianProfileModel model) {
        TechnicianProfileEntity entity = techRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Technician profile not found: " + id));

        assembler.updateEntityFromModel(model, entity);
        techRepo.save(entity);
        return assembler.toModel(entity);
    }

    @Override
    public void delete(Long id) {
        TechnicianProfileEntity entity = techRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Technician profile not found: " + id));
        techRepo.delete(entity);
    }
}
