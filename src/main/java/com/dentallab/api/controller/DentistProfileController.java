package com.dentallab.api.controller;

import com.dentallab.api.model.DentistProfileModel;
import com.dentallab.service.DentistProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Dentist Profiles.
 * Provides CRUD operations under /api/dentists.
 * Business logic and mapping are handled in the service layer.
 */
@RestController
@RequestMapping("/api/dentists")
public class DentistProfileController {

    private static final Logger log = LoggerFactory.getLogger(DentistProfileController.class);

    private final DentistProfileService dentistService;

    public DentistProfileController(DentistProfileService dentistService) {
        this.dentistService = dentistService;
    }

    /* ---------- GET all ---------- */
    @GetMapping
    public CollectionModel<DentistProfileModel> getAll() {
        log.debug("Incoming GET /api/dentists request");
        CollectionModel<DentistProfileModel> dentists = dentistService.getAll();
        log.info("GET /api/dentists returned {} dentist profiles", dentists.getContent().size());
        return dentists;
    }

    /* ---------- GET by ID ---------- */
    @GetMapping("/{id}")
    public ResponseEntity<DentistProfileModel> getById(@PathVariable Long id) {
        log.debug("Incoming GET /api/dentists/{} request", id);
        DentistProfileModel model = dentistService.getById(id);
        log.info("GET /api/dentists/{} succeeded", id);
        return ResponseEntity.ok(model);
    }

    /* ---------- CREATE ---------- */
    @PostMapping
    public ResponseEntity<DentistProfileModel> create(@RequestBody DentistProfileModel model) {
        log.debug("Incoming POST /api/dentists request for clientId={}", model.getClientId());
        DentistProfileModel created = dentistService.create(model);
        URI location = linkTo(methodOn(DentistProfileController.class).getById(created.getId())).toUri();
        log.info("Dentist profile created successfully with id={} (clientId={})", created.getId(), model.getClientId());
        return ResponseEntity.created(location).body(created);
    }

    /* ---------- UPDATE ---------- */
    @PutMapping("/{id}")
    public ResponseEntity<DentistProfileModel> update(@PathVariable Long id, @RequestBody DentistProfileModel model) {
        log.debug("Incoming PUT /api/dentists/{} request", id);
        DentistProfileModel updated = dentistService.update(id, model);
        log.info("Dentist profile updated successfully with id={}", id);
        return ResponseEntity.ok(updated);
    }

    /* ---------- DELETE ---------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Incoming DELETE /api/dentists/{} request", id);
        dentistService.delete(id);
        log.info("Dentist profile deleted successfully with id={}", id);
        return ResponseEntity.noContent().build();
    }
}
