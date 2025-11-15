package com.dentallab.api.controller;

import com.dentallab.api.model.StudentProfileModel;
import com.dentallab.service.StudentProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Student Profiles.
 * Provides CRUD endpoints under /api/students.
 * Business logic and mapping are handled in the service layer.
 */
@RestController
@RequestMapping("/api/students")
public class StudentProfileController {

    private static final Logger log = LoggerFactory.getLogger(StudentProfileController.class);

    private final StudentProfileService studentService;

    public StudentProfileController(StudentProfileService studentService) {
        this.studentService = studentService;
    }

    /* ---------- GET all ---------- */
    @GetMapping
    public CollectionModel<StudentProfileModel> getAll() {
        log.debug("Incoming GET /api/students request");
        CollectionModel<StudentProfileModel> students = studentService.getAll();
        log.info("GET /api/students returned {} student profiles", students.getContent().size());
        return students;
    }

    /* ---------- GET by ID ---------- */
    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileModel> getById(@PathVariable Long id) {
        log.debug("Incoming GET /api/students/{} request", id);
        StudentProfileModel model = studentService.getById(id);
        log.info("GET /api/students/{} succeeded", id);
        return ResponseEntity.ok(model);
    }

    /* ---------- CREATE ---------- */
    @PostMapping
    public ResponseEntity<StudentProfileModel> create(@RequestBody StudentProfileModel model) {
        log.debug("Incoming POST /api/students request for clientId={}", model.getClientId());
        StudentProfileModel created = studentService.create(model);
        URI location = linkTo(methodOn(StudentProfileController.class).getById(created.getId())).toUri();
        log.info("Student profile created successfully with id={} (clientId={})", created.getId(), model.getClientId());
        return ResponseEntity.created(location).body(created);
    }

    /* ---------- UPDATE ---------- */
    @PutMapping("/{id}")
    public ResponseEntity<StudentProfileModel> update(@PathVariable Long id, @RequestBody StudentProfileModel model) {
        log.debug("Incoming PUT /api/students/{} request", id);
        StudentProfileModel updated = studentService.update(id, model);
        log.info("Student profile updated successfully with id={}", id);
        return ResponseEntity.ok(updated);
    }

    /* ---------- DELETE ---------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Incoming DELETE /api/students/{} request", id);
        studentService.delete(id);
        log.info("Student profile deleted successfully with id={}", id);
        return ResponseEntity.noContent().build();
    }
}
