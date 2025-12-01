package com.dentallab.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.MaterialAssembler;
import com.dentallab.api.model.MaterialModel;
import com.dentallab.persistence.entity.MaterialEntity;
import com.dentallab.persistence.repository.MaterialRepository;
import com.dentallab.service.MaterialService;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    private static final Logger log = LoggerFactory.getLogger(MaterialServiceImpl.class);

    private final MaterialRepository repo;
    private final MaterialAssembler assembler;

    public MaterialServiceImpl(MaterialRepository repo, MaterialAssembler assembler) {
        this.repo = repo;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialModel> getAllPaged(int page, int size) {
        return repo.findAll(PageRequest.of(page, size))
                   .map(assembler::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialModel getById(Long id) {
        MaterialEntity e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found " + id));

        return assembler.toModel(e);
    }

    @Override
    public MaterialModel create(MaterialModel model) {
        MaterialEntity e = new MaterialEntity();
        updateEntity(model, e);

        e = repo.save(e);

        log.info("Created Material id={} name={}", e.getId(), e.getName());

        return assembler.toModel(e);
    }

    @Override
    public MaterialModel update(Long id, MaterialModel model) {
        MaterialEntity e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found " + id));

        updateEntity(model, e);
        repo.save(e);

        log.info("Updated Material id={}", e.getId());
        return assembler.toModel(e);
    }
    
    private void updateEntity(MaterialModel m, MaterialEntity e) {
        e.setName(m.getName());
        e.setCategory(m.getCategory());
        e.setUnit(m.getUnit());
        e.setPricePerUnit(m.getPricePerUnit());
        e.setStatus(m.getStatus());
        e.setNotes(m.getNotes());
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
        log.info("Deleted Material id={}", id);
    }
}
