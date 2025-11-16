package com.dentallab.api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.service.WorkService;
import com.dentallab.util.PagingUtils;
import com.dentallab.util.SortValidationUtils;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private static final Logger log = LoggerFactory.getLogger(WorkController.class);

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    // ==========================================================
    // FILTERED, PAGINATED GET
    // ==========================================================
    @GetMapping
    public ResponseEntity<Page<WorkModel>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String type,      // code
            @RequestParam(required = false) String status,    // code
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String family,    // code
            @RequestParam(required = false) List<String> categories
    ) {

        String[] sortParams = PagingUtils.parseSortParameter(sort);
        SortValidationUtils.validateWorkSortFields(sortParams);

        log.debug("Fetching works page={} size={} sort={} filters: type={} family={} status={} client={} categories={}",
                page, size, sort, type, family, status, clientId, categories);

        Page<WorkModel> worksPage =
                workService.getFilteredWithClients(page, size, sortParams, type, status, clientId, family, categories);

        return ResponseEntity.ok(worksPage);
    }

    // Legacy overload for HATEOAS links (required!)
    public ResponseEntity<?> getAll(int page, int size, String sort) {
        return getAll(page, size, sort, null, null, null, null, null);
    }

    // Simplified no-filter version
    @GetMapping("/simple")
    public ResponseEntity<?> getAllSimple(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return getAll(page, size, sort, null, null, null, null, null);
    }

    // ==========================================================
    // Basic CRUD
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<WorkModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkModel> create(@RequestBody WorkModel model) {
        WorkModel created = workService.create(model);
        URI location = linkTo(methodOn(WorkController.class).getById(created.getId())).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkModel> update(@PathVariable Long id, @RequestBody WorkModel model) {
        return ResponseEntity.ok(workService.update(id, model));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // Full (Base + Extension)
    // ==========================================================
    @GetMapping("/{id}/full")
    public ResponseEntity<FullWorkModel> getFullWork(@PathVariable Long id) {
        return ResponseEntity.ok(workService.getFullById(id));
    }
}
