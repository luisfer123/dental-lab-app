package com.dentallab.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dentallab.api.model.MaterialModel;
import com.dentallab.service.MaterialService;

@RestController
@RequestMapping(value = "/api/materials", produces = "application/json")
public class MaterialController {

    private static final Logger log = LoggerFactory.getLogger(MaterialController.class);

    private final MaterialService service;

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<MaterialModel>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/materials?page={}&size={}", page, size);
        return ResponseEntity.ok(service.getAllPaged(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialModel> getById(@PathVariable Long id) {
        log.debug("GET /api/materials/{}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<MaterialModel> create(@RequestBody MaterialModel model) {
        log.debug("POST /api/materials");
        return ResponseEntity.ok(service.create(model));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<MaterialModel> update(@PathVariable Long id, @RequestBody MaterialModel model) {
        log.debug("PUT /api/materials/{}", id);
        return ResponseEntity.ok(service.update(id, model));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("DELETE /api/materials/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
