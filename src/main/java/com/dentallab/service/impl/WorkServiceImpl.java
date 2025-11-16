package com.dentallab.service.impl;

import static com.dentallab.persistence.spec.WorkSpecifications.hasCategoryNames;
import static com.dentallab.persistence.spec.WorkSpecifications.hasStatus;
import static com.dentallab.persistence.spec.WorkSpecifications.hasType;
import static com.dentallab.persistence.spec.WorkSpecifications.hasWorkFamily;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.FullWorkAssembler;
import com.dentallab.api.assembler.WorkAssembler;
import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.WorkRepository;
import com.dentallab.service.WorkService;
import com.dentallab.util.PagingUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class WorkServiceImpl implements WorkService {

    private static final Logger log = LoggerFactory.getLogger(WorkServiceImpl.class);

    private final WorkRepository workRepository;
    private final WorkAssembler workAssembler;
    private final FullWorkAssembler fullWorkAssembler;

    public WorkServiceImpl(
            WorkRepository workRepository,
            WorkAssembler workAssembler,
            FullWorkAssembler fullWorkAssembler
    ) {
        this.workRepository = workRepository;
        this.workAssembler = workAssembler;
        this.fullWorkAssembler = fullWorkAssembler;
    }

    // ==========================================================
    // BASIC CRUD
    // ==========================================================

    @SuppressWarnings("removal")
	@Override
    @Transactional(readOnly = true)
    public List<WorkModel> getAll(int page, int size, String typeCode) {

        Pageable pageable = PageRequest.of(page, size);

        Page<WorkEntity> resultPage;

        if (typeCode != null && !typeCode.isBlank()) {
            // filtering by type code → lookup-safe
            Specification<WorkEntity> spec = Specification.where(hasType(typeCode));
            resultPage = workRepository.findAll(spec, pageable);
        } else {
            resultPage = workRepository.findAll(pageable);
        }

        return resultPage.map(workAssembler::toModel).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkModel getById(Long id) {
        WorkEntity entity = workRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work not found with id " + id));

        return workAssembler.toModel(entity);
    }

    @Override
    @Transactional
    public WorkModel create(WorkModel model) {
        WorkEntity entity = workAssembler.toEntity(model);

        WorkEntity saved = workRepository.save(entity);

        log.info("Created Work: id={}, type={}, family={}",
                saved.getId(),
                saved.getType() != null ? saved.getType().getCode() : null,
                saved.getWorkFamily() != null ? saved.getWorkFamily().getCode() : null
        );

        return workAssembler.toModel(saved);
    }

    @Override
    @Transactional
    public WorkModel update(Long id, WorkModel model) {
        WorkEntity existing = workRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work not found with id " + id));

        workAssembler.updateEntityFromModel(model, existing);

        WorkEntity updated = workRepository.save(existing);

        log.info("Updated Work: id={}, type={}, family={}",
                updated.getId(),
                updated.getType() != null ? updated.getType().getCode() : null,
                updated.getWorkFamily() != null ? updated.getWorkFamily().getCode() : null
        );

        return workAssembler.toModel(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!workRepository.existsById(id)) {
            throw new EntityNotFoundException("Work not found with id " + id);
        }

        workRepository.deleteById(id);

        log.info("Deleted Work id={}", id);
    }

    // ==========================================================
    // FULL WORK VIEW
    // ==========================================================

    @Override
    @Transactional(readOnly = true)
    public FullWorkModel getFullById(Long id) {
        WorkEntity entity = workRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work not found with id " + id));

        return fullWorkAssembler.toModel(entity);
    }

    // ==========================================================
    // FILTERED + PAGINATED QUERY
    // ==========================================================

    @SuppressWarnings("removal")
	@Override
    @Transactional(readOnly = true)
    public Page<WorkModel> getFiltered(
            int page,
            int size,
            String[] sortParams,
            String typeCode,
            String statusCode,
            Long clientId,
            String familyCode,
            List<String> categories
    ) {
        Sort sort = PagingUtils.parseSort(sortParams);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<WorkEntity> spec = Specification
                .where(hasType(typeCode))
                .and(hasStatus(statusCode))
                .and(hasWorkFamily(familyCode))
                .and(hasCategoryNames(categories));

        if (clientId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("client").get("id"), clientId));
        }

        Page<WorkEntity> entityPage = workRepository.findAll(spec, pageable);

        Page<WorkModel> modelPage = entityPage.map(workAssembler::toModel);

        log.info("Filtered work query: count={} page={}/{} filters=[type={}, family={}, status={}, client={}, categories={}]",
                modelPage.getNumberOfElements(),
                page + 1,
                modelPage.getTotalPages(),
                typeCode, familyCode, statusCode, clientId, categories);

        return modelPage;
    }
    
    /**
     * Returns a paginated, dynamically filtered list of works including their associated clients.
     * 
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sortParams  Sort array, e.g. ["createdAt,desc"]
     * @param typeCode    Work type code (optional)
     * @param statusCode  Work status (optional)
     * @param clientId    Associated client ID (optional)
     * @param familyCode  Work family code (optional)
     * @param categories  List of category names to match (optional)
     * 
     * @return Paginated page of filtered WorkModel with clients
     */
    @SuppressWarnings("removal")
	@Override
    @Transactional(readOnly = true)
    public Page<WorkModel> getFilteredWithClients(
            int page,
            int size,
            String[] sortParams,
            String typeCode,
            String statusCode,
            Long clientId,
            String familyCode,
            List<String> categories
    ) {
        log.debug(
            "Starting filtered work query (with clients): page={}, size={}, sort={}, "
            + "filters: type={}, status={}, family={}, clientId={}, categories={}",
            page, size, Arrays.toString(sortParams),
            typeCode, statusCode, familyCode, clientId, categories
        );

        Sort sort = PagingUtils.parseSort(sortParams);
        Pageable pageable = PageRequest.of(page, size, sort);

        // ---------------------------------------------------------
        // Build specification
        // ---------------------------------------------------------
        Specification<WorkEntity> spec = Specification
                .where(hasType(typeCode))
                .and(hasStatus(statusCode))
                .and(hasWorkFamily(familyCode))
                .and(hasCategoryNames(categories));

        if (clientId != null) {
            log.debug("Applying client filter: clientId={}", clientId);
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("client").get("id"), clientId));
        }

        // ---------------------------------------------------------
        // Step 1: Page of IDs
        // ---------------------------------------------------------
        Page<Long> idPage = workRepository.findAll(spec, pageable)
                .map(WorkEntity::getId);

        log.debug("ID page fetched: count={}, totalElements={}, totalPages={}",
                idPage.getNumberOfElements(),
                idPage.getTotalElements(),
                idPage.getTotalPages());

        if (idPage.isEmpty()) {
            log.info("Filtered query returned no results.");
            return Page.empty(pageable);
        }

        // ---------------------------------------------------------
        // Step 2: Fetch works + client in one optimized query
        // ---------------------------------------------------------
        List<Long> ids = idPage.getContent();
        log.debug("Fetching {} works with clients via join…", ids.size());

        List<WorkEntity> entities =
                workRepository.findAllWithClientByIdIn(ids);

        log.debug("Fetch query returned {} entities.", entities.size());

        // ---------------------------------------------------------
        // Step 3: Preserve order
        // ---------------------------------------------------------
        List<WorkModel> models = ids.stream()
                .map(id -> entities.stream()
                        .filter(w -> w.getId().equals(id))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(workAssembler::toModel)
                .toList();

        log.debug("Mapped {} entities → models, preserving order.", models.size());

        // ---------------------------------------------------------
        // Return result
        // ---------------------------------------------------------
        log.info("Returning filtered+client page: page={} of {} ({} items)",
                page + 1, idPage.getTotalPages(), models.size());

        return new PageImpl<>(models, pageable, idPage.getTotalElements());
    }


}
