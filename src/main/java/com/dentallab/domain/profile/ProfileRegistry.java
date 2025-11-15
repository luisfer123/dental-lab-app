package com.dentallab.domain.profile;

import com.dentallab.api.model.ProfileModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileRegistry {

    private final List<ProfileFactory> factories;

    public ProfileRegistry(List<ProfileFactory> factories) {
        this.factories = factories;
    }

    public List<ProfileModel> loadProfiles(Long clientId) {
        return factories.stream()
                .map(f -> f.createForClient(clientId))
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}

//ProfileRegistry acts as a proxy, mediator, and lightweight orchestrator 
//between the Factory Method pattern and the rest of the application 
//(like your service and assemblers)

// Factories are injected into ProfileRegistry by the Spring container

//				+--------------------+
//				|  ProfileRegistry   |  ← Mediator / Proxy
//				+--------------------+
//				| +loadProfiles()    |
//				+---------+----------+
//				          |
//				-------------------------
//				|           |           |
//	+---------------+ +---------------+ +---------------+
//	| DentistFactory| | StudentFactory| | TechnicianFactory|
//	+---------------+ +---------------+ +---------------+
//	| createForClient() | ... | createForClient() |
//	|           |           |
//	v           v           v
//	DentistModel  StudentModel  TechnicianModel