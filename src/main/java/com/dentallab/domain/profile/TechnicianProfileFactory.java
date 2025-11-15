package com.dentallab.domain.profile;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.dentallab.api.assembler.TechnicianProfileAssembler;
import com.dentallab.api.model.ProfileModel;
import com.dentallab.persistence.repository.TechnicianProfileRepository;

@Component
public class TechnicianProfileFactory implements ProfileFactory {
	
	private final TechnicianProfileRepository repo;
	private final TechnicianProfileAssembler assembler;
	
	public TechnicianProfileFactory(TechnicianProfileRepository repo, TechnicianProfileAssembler assembler) {
		this.repo = repo;
		this.assembler = assembler;
	}
	
	@Override
	public Optional<ProfileModel> createForClient(Long clientId) {
		return repo.findByClient_Id(clientId)
				.map(assembler::toModel);
		
	}

}
