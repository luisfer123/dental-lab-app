package com.dentallab.service;

import com.dentallab.api.model.DentistProfileModel;
import org.springframework.hateoas.CollectionModel;

public interface DentistProfileService {

    CollectionModel<DentistProfileModel> getAll();

    DentistProfileModel getById(Long id);

    DentistProfileModel create(DentistProfileModel model);

    DentistProfileModel update(Long id, DentistProfileModel model);

    void delete(Long id);
}
