package com.dentallab.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dentallab.api.model.ClientModel;
import com.dentallab.service.ClientService;

@RestController
@RequestMapping("/api/client-profiles")
public class ClientProfileController {

	private static final Logger log = LoggerFactory.getLogger(ClientProfileController.class);
	private final ClientService clientService;

	public ClientProfileController(ClientService clientService) {
		this.clientService = clientService;
	}

	// ============================================================
	// DENTISTS
	// ============================================================
	@GetMapping("/dentists")
	public ResponseEntity<Page<ClientModel>> getAllDentists(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "displayName,asc") String sort) {

		// Split the "field,direction" string manually
		String[] sortParams = { sort };

		log.debug("GET /api/client-profiles/dentists?page={}&size={}&sort={}", page, size, (Object) sort);
		Page<ClientModel> dentistsPage = clientService.getAllWithDentistProfile(page, size, sortParams);

		log.info("Fetched {} dentists (page {}/{})", dentistsPage.getNumberOfElements(), dentistsPage.getNumber() + 1,
				dentistsPage.getTotalPages());

		return ResponseEntity.ok(dentistsPage);
	}

	// ============================================================
	// STUDENTS
	// ============================================================
	@GetMapping("/students")
	public ResponseEntity<Page<ClientModel>> getAllStudents(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "displayName,asc") String sort) {

		// Split the "field,direction" string manually
		String[] sortParams = { sort };

		log.debug("GET /api/client-profiles/students?page={}&size={}&sort={}", page, size, (Object) sort);
		Page<ClientModel> studentsPage = clientService.getAllWithStudentProfile(page, size, sortParams);

		log.info("Fetched {} students (page {}/{})", 
				studentsPage.getNumberOfElements(), 
				studentsPage.getNumber() + 1,
				studentsPage.getTotalPages());

		return ResponseEntity.ok(studentsPage);
	}

	// ============================================================
	// TECHNICIANS
	// ============================================================
	@GetMapping("/technicians")
	public ResponseEntity<Page<ClientModel>> getAllTechnicians(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(defaultValue = "displayName,asc") String sort) {

	    String[] sortParams = { sort };

	    log.debug("GET /api/client-profiles/technicians?page={}&size={}&sort={}", page, size, (Object) sort);
	    Page<ClientModel> techniciansPage = clientService.getAllWithTechnicianProfile(page, size, sortParams);

	    log.info("Fetched {} technicians (page {}/{})",
	            techniciansPage.getNumberOfElements(),
	            techniciansPage.getNumber() + 1,
	            techniciansPage.getTotalPages());

	    return ResponseEntity.ok(techniciansPage);
	}


}
