package com.dentallab.service.impl;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.ClientAssembler;
import com.dentallab.api.assembler.ClientFullModelAssembler;
import com.dentallab.api.assembler.ClientSummaryAssembler;
import com.dentallab.api.model.ClientFullModel;
import com.dentallab.api.model.ClientModel;
import com.dentallab.api.model.ClientSummaryModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.service.ClientService;
import com.dentallab.util.PagingUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ClientServiceImpl implements ClientService {

	private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

	private final ClientRepository clientRepo;
	private final ClientAssembler assembler;
	private final ClientFullModelAssembler fullAssembler;
	private final ClientSummaryAssembler summaryAssembler;

	public ClientServiceImpl(
			ClientRepository clientRepo, 
			ClientAssembler assembler,
			ClientFullModelAssembler fullAssembler,
			ClientSummaryAssembler summaryAssembler) {
		this.clientRepo = clientRepo;
		this.assembler = assembler;
		this.fullAssembler = fullAssembler;
		this.summaryAssembler = summaryAssembler;
	}

	/* -------------------- READ ALL -------------------- */
	@Override
	@Transactional(readOnly = true)
	public CollectionModel<ClientModel> getAll() {
		log.debug("Fetching all clients from database...");
		List<ClientModel> models = clientRepo.findAll().stream().map(assembler::toModel).toList();
		log.info("Retrieved {} clients", models.size());
		return CollectionModel.of(models);
	}

	/* -------------------- READ ALL PAGED -------------------- */
	@Override
	@Transactional(readOnly = true)
	public Page<ClientModel> getAllPaged(int page, int size, String[] sortParams) {
		// Parse sort parameters like "displayName,asc"
		Sort sort = PagingUtils.parseSort(sortParams);
		Pageable pageable = PageRequest.of(page, size, sort);

		log.debug("Fetching paginated clients: page={}, size={}, sort={}", page, size, sort);

		Page<ClientEntity> pageResult = clientRepo.findAll(pageable);

		// Convert entities to models
		Page<ClientModel> modelPage = pageResult.map(assembler::toModel);

		log.info("Fetched {} clients on page {}/{}", modelPage.getNumberOfElements(), modelPage.getNumber() + 1,
				modelPage.getTotalPages());

		return modelPage;
	}

	/* -------------------- READ ONE -------------------- */
	@Override
	@Transactional(readOnly = true)
	public ClientModel getById(Long id) {
		log.debug("Fetching client with id={}", id);
		ClientEntity entity = clientRepo.findById(id).orElseThrow(() -> {
			log.warn("Client not found with id={}", id);
			return new EntityNotFoundException("Client not found with id=" + id);
		});
		log.info("Client found: {}", entity.getDisplayName());
		return assembler.toModel(entity);
	}

	/* -------------------- READ FULL -------------------- */
	@Override
	@Transactional(readOnly = true)
	public ClientFullModel getFullById(Long clientId) {
		log.debug("Fetching full client with id={}", clientId);
		ClientEntity client = clientRepo.findById(clientId).orElseThrow(() -> {
			log.warn("Client not found with id={}", clientId);
			return new EntityNotFoundException("Client not found with id=" + clientId);
		});
		log.info("Client found: {}", client.getDisplayName());
		return fullAssembler.toModel(client);
	}
	
	/**
	 * Search clients by name, email, or phone.
	 * 
	 * @param query the search query string
	 * @param page  the page number (0-based)
	 * @param size  the page size
	 * 
	 * @return a page of matching ClientSummaryModel
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<ClientSummaryModel> searchClients(String query, int page, int size) {
		
		log.debug("Searching clients with query='{}', page={}, size={}", query, page, size);
		Pageable pageable = PageRequest.of(page, size);
		log.info("Executing search in repository...");
		Page<ClientEntity> pageResult = 
				clientRepo.searchByNameEmailPhone(query, pageable);
		Page<ClientSummaryModel> pageModel = 
				pageResult.map(summaryAssembler::toModel);
		
		log.info("Search returned {} clients on page {}/{} for query='{}'",
				pageModel.getNumberOfElements(),
				pageModel.getNumber() + 1,
				pageModel.getTotalPages(),
				query);
		
	    return pageModel;
	}

	/**
	 * Create a new client.
	 * 
	 * @param model the client model to create
	 * @return the created client model with generated ID
	 */
	@Override
	@Transactional
	public ClientModel create(ClientModel model) {
		log.info("Creating new client: {}", model.getDisplayName());
		ClientEntity entity = assembler.toEntity(model);
		ClientEntity saved = clientRepo.save(entity);
		log.info("Client created successfully with id={}", saved.getId());
		return assembler.toModel(saved);
	}

	/* -------------------- UPDATE -------------------- */
	@Override
	@Transactional
	public ClientModel update(Long id, ClientModel model) {
		log.debug("Updating client with id={}", id);
		ClientEntity existing = clientRepo.findById(id).orElseThrow(() -> {
			log.warn("Cannot update: client not found with id={}", id);
			return new EntityNotFoundException("Client not found with id=" + id);
		});

		assembler.updateEntityFromModel(model, existing);
		ClientEntity saved = clientRepo.save(existing);
		log.info("Client updated successfully: id={}", id);
		return assembler.toModel(saved);
	}

	/* -------------------- DELETE -------------------- */
	@Override
	@Transactional
	public void delete(Long id) {
		log.debug("Attempting to delete client with id={}", id);
		if (!clientRepo.existsById(id)) {
			log.warn("Cannot delete: client not found with id={}", id);
			throw new EntityNotFoundException("Client not found with id=" + id);
		}
		clientRepo.deleteById(id);
		log.info("Client deleted successfully: id={}", id);
	}
	
	/* -------------------- RETURN CLIENTS WITH PROFILE PAGED  -------------------- */
	
	@Transactional(readOnly = true)
	private Page<ClientModel> fetchClientsByProfile(
	        Function<Pageable, Page<ClientEntity>> queryFunction,
	        int page, int size, String[] sortParams, String profileName) {

		if (sortParams == null) sortParams = new String[0];
	    Sort sort = PagingUtils.parseSort(sortParams);
	    Pageable pageable = PageRequest.of(page, size, sort);

	    log.debug("Fetching paginated clients with profile {}: page={}, size={}, sort={}", profileName, page, size, sort);

	    Page<ClientEntity> pageResult = queryFunction.apply(pageable);
	    Page<ClientModel> modelPage = pageResult.map(assembler::toModel);

	    log.info("Fetched {} clients with {} profile on page {}/{}",
	            modelPage.getNumberOfElements(), profileName,
	            modelPage.getNumber() + 1, modelPage.getTotalPages());

	    return modelPage;
	}


	@Override
	public Page<ClientModel> getAllWithDentistProfile(int page, int size, String[] sortParams) {
		return fetchClientsByProfile(clientRepo::findAllWithDentistProfile, page, size, sortParams, "dentist");
	}

	@Override
	public Page<ClientModel> getAllWithStudentProfile(int page, int size, String[] sortParams) {
		return fetchClientsByProfile(clientRepo::findAllWithStudentProfile, page, size, sortParams, "student");
	}

	@Override
	public Page<ClientModel> getAllWithTechnicianProfile(int page, int size, String[] sortParams) {
		return fetchClientsByProfile(clientRepo::findAllWithTechnicianProfile, page, size, sortParams, "technician");
	}

}
