package com.dentallab.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dentallab.api.assembler.TechnicianProfileAssembler;
import com.dentallab.api.model.TechnicianProfileModel;
import com.dentallab.service.TechnicianProfileService;

/**
 * REST Controller for managing Technician Profiles.
 */
@RestController
@RequestMapping("/api/technician-profiles")
public class TechnicianProfileController {

    private static final Logger log = LoggerFactory.getLogger(TechnicianProfileController.class);

    private final TechnicianProfileService techService;
    public TechnicianProfileController(TechnicianProfileService techService, TechnicianProfileAssembler assembler) {
        this.techService = techService;
    }

    // ============================================================
    // GET ALL (Paginated)
    // ============================================================
    @GetMapping
    public ResponseEntity<Page<TechnicianProfileModel>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "labName,asc") String sort) {

        String[] sortParams = { sort };
        log.debug("GET /api/technician-profiles?page={}&size={}&sort={}", page, size, (Object) sort);
        Page<TechnicianProfileModel> technicians = techService.getAll(page, size, sortParams);
        log.info("Fetched {} technician profiles (page {}/{})",
                technicians.getNumberOfElements(), technicians.getNumber() + 1, technicians.getTotalPages());
        return ResponseEntity.ok(technicians);
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianProfileModel> getById(@PathVariable Long id) {
        log.debug("GET /api/technician-profiles/{}", id);
        TechnicianProfileModel model = techService.getById(id);
        return ResponseEntity.ok(model);
    }

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<TechnicianProfileModel> create(@RequestBody TechnicianProfileModel model) {
        log.debug("POST /api/technician-profiles - Creating profile for client {}", model.getClientId());
        TechnicianProfileModel created = techService.create(model);
        URI location = linkTo(methodOn(TechnicianProfileController.class).getById(created.getId())).toUri();
        log.info("Created technician profile id={} for client={}", created.getId(), model.getClientId());
        return ResponseEntity.created(location).body(created);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianProfileModel> update(@PathVariable Long id, @RequestBody TechnicianProfileModel model) {
        log.debug("PUT /api/technician-profiles/{}", id);
        TechnicianProfileModel updated = techService.update(id, model);
        log.info("Updated technician profile id={}", id);
        return ResponseEntity.ok(updated);
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("DELETE /api/technician-profiles/{}", id);
        techService.delete(id);
        log.info("Deleted technician profile id={}", id);
        return ResponseEntity.noContent().build();
    }
}
