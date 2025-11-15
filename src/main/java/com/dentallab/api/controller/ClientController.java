package com.dentallab.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dentallab.api.model.ClientFullModel;
import com.dentallab.api.model.ClientModel;
import com.dentallab.service.ClientService;

/**
 * REST controller for Client resources.
 * Exposes endpoints under /api/clients.
 * Delegates all business logic to the ClientService.
 */
@RestController
@RequestMapping(
    value = "/api/clients",
    produces = "application/json"
)
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    /* -------------------- READ ALL (with pagination) -------------------- */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayName,asc") String sort) {
    	
    	// Split the "field,direction" string manually
        String[] sortParams = { sort };

        log.debug("GET /api/clients?page={}&size={}&sort={}", page, size, (Object) sort);
        Page<ClientModel> clientsPage = clientService.getAllPaged(page, size, sortParams);
        log.info("GET /api/clients returned {} clients", clientsPage.getContent().size());
        return ResponseEntity.ok(clientsPage);
    }

    /* -------------------- READ ONE -------------------- */
    @GetMapping("/{id}")
    public ResponseEntity<ClientModel> getById(@PathVariable Long id) {
        log.debug("GET /api/clients/{} - fetching client", id);
        ClientModel model = clientService.getById(id);
        log.info("GET /api/clients/{} succeeded", id);
        return ResponseEntity.ok(model);
    }

    /* -------------------- READ FULL -------------------- */
    @GetMapping("/{id}/full")
    public ResponseEntity<ClientFullModel> getClientFull(@PathVariable Long id) {
        log.debug("GET /api/clients/{}/full - fetching full client info", id);
        ClientFullModel model = clientService.getFullById(id);
        model.add(linkTo(methodOn(ClientController.class).getClientFull(id)).withSelfRel());
        log.info("GET /api/clients/{}/full succeeded", id);
        return ResponseEntity.ok(model);
    }

    /* -------------------- CREATE -------------------- */
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClientModel> create(@RequestBody ClientModel model) {
        log.debug("POST /api/clients - creating client '{}'", model.getDisplayName());
        ClientModel created = clientService.create(model);
        URI location = linkTo(methodOn(ClientController.class).getById(created.getId())).toUri();
        log.info("POST /api/clients - created id={} (Location: {})", created.getId(), location);
        return ResponseEntity.created(location).body(created);
    }

    /* -------------------- UPDATE -------------------- */
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ClientModel> update(@PathVariable Long id, @RequestBody ClientModel model) {
        log.debug("PUT /api/clients/{} - updating client", id);
        ClientModel updated = clientService.update(id, model);
        log.info("PUT /api/clients/{} - updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    /* -------------------- DELETE -------------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("DELETE /api/clients/{} - deleting client", id);
        clientService.delete(id);
        log.info("DELETE /api/clients/{} - deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
