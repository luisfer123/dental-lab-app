package com.dentallab.service;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;

import com.dentallab.api.model.ClientFullModel;
import com.dentallab.api.model.ClientModel;
import com.dentallab.api.model.ClientSummaryModel;

public interface ClientService {

	CollectionModel<ClientModel> getAll();

	Page<ClientModel> getAllPaged(int page, int size, String[] sort);

	ClientModel getById(Long id);

	ClientModel create(ClientModel model);

	ClientModel update(Long id, ClientModel model);

	ClientFullModel getFullById(Long clientId);

	void delete(Long id);

	// =============================
	// FILTERS BY PROFILE
	// =============================
	Page<ClientModel> getAllWithDentistProfile(int page, int size, String[] sortParams);
	
	Page<ClientModel> getAllWithStudentProfile(int page, int size, String[] sortParams);
	
	Page<ClientModel> getAllWithTechnicianProfile(int page, int size, String[] sortParams);

	// =============================
	// SEARCH
	// =============================
	Page<ClientSummaryModel> searchClients(String query, int page, int size);
}
