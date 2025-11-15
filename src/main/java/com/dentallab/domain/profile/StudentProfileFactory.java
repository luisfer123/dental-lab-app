package com.dentallab.domain.profile;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dentallab.api.assembler.StudentProfileAssembler;
import com.dentallab.api.model.ProfileModel;
import com.dentallab.persistence.repository.StudentProfileRepository;

@Component
public class StudentProfileFactory implements ProfileFactory {

    private final StudentProfileRepository repo;
    private final StudentProfileAssembler assembler;

    public StudentProfileFactory(StudentProfileRepository repo, StudentProfileAssembler assembler) {
        this.repo = repo;
        this.assembler = assembler;
    }

    @Override
    public Optional<ProfileModel> createForClient(Long clientId) {
        return repo.findByClient_Id(clientId).map(assembler::toModel);
    }
}
