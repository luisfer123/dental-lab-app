package com.dentallab.service;

import org.springframework.data.domain.Page;

import com.dentallab.api.model.MaterialModel;

public interface MaterialService {

    Page<MaterialModel> getAllPaged(int page, int size);

    MaterialModel getById(Long id);

    MaterialModel create(MaterialModel model);

    MaterialModel update(Long id, MaterialModel model);

    void delete(Long id);
}
