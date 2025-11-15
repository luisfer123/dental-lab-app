package com.dentallab.service;

import org.springframework.data.domain.Page;

import com.dentallab.api.model.TechnicianProfileModel;

public interface TechnicianProfileService {
    Page<TechnicianProfileModel> getAll(int page, int size, String[] sortParams);
    TechnicianProfileModel getById(Long id);
    TechnicianProfileModel create(TechnicianProfileModel model);
    TechnicianProfileModel update(Long id, TechnicianProfileModel model);
    void delete(Long id);
}
