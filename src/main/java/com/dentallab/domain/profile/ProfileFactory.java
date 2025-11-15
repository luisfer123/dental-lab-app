package com.dentallab.domain.profile;

import com.dentallab.api.model.ProfileModel;

import java.util.Optional;

public interface ProfileFactory {
    /**
     * Crea el ProfileModel correspondiente para el clientId dado, si existe.
     */
    Optional<ProfileModel> createForClient(Long clientId);
}


//[HTTP Request]
//		   |
//		   v
//		ClientController.getClientFull(id)
//		   |
//		   v
//		ClientServiceImpl.getFullById(id)
//		   |
//		   v
//		ClientFullModelAssembler.toModel(ClientEntity)
//		   |
//		   v
//		ProfileRegistry.loadProfiles(clientId)
//		   |
//		   ├── DentistProfileFactory.createForClient(id)
//		   │       └── DentistProfileRepository.findByClientId()
//		   │       └── DentistProfileAssembler.toModel()
//		   │
//		   ├── StudentProfileFactory.createForClient(id)
//		   │       └── StudentProfileRepository.findByClientId()
//		   │       └── StudentProfileAssembler.toModel()
//		   │
//		   └── TechnicianProfileFactory.createForClient(id)
//		           └── TechnicianProfileRepository.findByClientId()
//		           └── TechnicianProfileAssembler.toModel()
//		   |
//		   v
//		[Profiles returned as List<ProfileModel>]
//		   |
//		   v
//		Assembler adds them to ClientFullModel.profiles[]
//		   |
//		   v
//		HATEOAS self link added
//		   |
//		   v
//		[JSON Response to Client]