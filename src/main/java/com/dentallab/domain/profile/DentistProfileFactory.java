package com.dentallab.domain.profile;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dentallab.api.assembler.DentistProfileAssembler;
import com.dentallab.api.model.ProfileModel;
import com.dentallab.persistence.repository.DentistProfileRepository;

@Component
public class DentistProfileFactory implements ProfileFactory {

    private final DentistProfileRepository repo;
    private final DentistProfileAssembler assembler;

    public DentistProfileFactory(DentistProfileRepository repo, DentistProfileAssembler assembler) {
        this.repo = repo;
        this.assembler = assembler;
    }

    @Override
    public Optional<ProfileModel> createForClient(Long clientId) {
        return repo.findByClient_Id(clientId)
        		.map(assembler::toModel);
    }
}
