package com.dentallab.service;

import com.dentallab.api.model.StudentProfileModel;
import org.springframework.hateoas.CollectionModel;

public interface StudentProfileService {

    CollectionModel<StudentProfileModel> getAll();

    StudentProfileModel getById(Long id);

    StudentProfileModel create(StudentProfileModel model);

    StudentProfileModel update(Long id, StudentProfileModel model);

    void delete(Long id);
}
