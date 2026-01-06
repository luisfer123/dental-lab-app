package com.dentallab.service.impl;

import static com.dentallab.persistence.spec.WorkSpecifications.hasCategoryNames;
import static com.dentallab.persistence.spec.WorkSpecifications.hasStatus;
import static com.dentallab.persistence.spec.WorkSpecifications.hasType;
import static com.dentallab.persistence.spec.WorkSpecifications.hasWorkFamily;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.dentallab.api.assembler.BridgeWorkAssembler;
import com.dentallab.api.assembler.FullWorkAssembler;
import com.dentallab.api.assembler.WorkAssembler;
import com.dentallab.api.model.BridgeWorkModel;
import com.dentallab.api.model.CrownWorkModel;
import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkExtensionModel;
import com.dentallab.api.model.WorkModel;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;
import com.dentallab.persistence.entity.WorkStatusRefEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkOrderRepository;
import com.dentallab.persistence.repository.WorkRepository;
import com.dentallab.persistence.repository.WorkStatusRefRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;
import com.dentallab.service.CrownWorkService;
import com.dentallab.service.WorkService;
import com.dentallab.util.PagingUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class WorkServiceImpl implements WorkService {

    private static final Logger log = LoggerFactory.getLogger(WorkServiceImpl.class);

    private final WorkRepository workRepository;
    
    private final ClientRepository clientRepository;
    
    private final WorkTypeRefRepository workTypeRefRepository;
    private final WorkFamilyRefRepository workFamilyRefRepository;
    private final WorkStatusRefRepository statusRefRepository;
    
    private final WorkOrderRepository orderRepository;
    
    private final CrownWorkService crownWorkService;
    
    private final WorkAssembler workAssembler;
    private final FullWorkAssembler fullWorkAssembler;

    public WorkServiceImpl(
            WorkRepository workRepository,
            WorkAssembler workAssembler,
            FullWorkAssembler fullWorkAssembler,
            ClientRepository clientRepository,
            WorkTypeRefRepository workTypeRefRepository,
            WorkFamilyRefRepository workFamilyRefRepository,
            CrownWorkService crownWorkService,
            WorkOrderRepository orderRepository,
            WorkStatusRefRepository statusRefRepository
    ) {
        this.workRepository = workRepository;
        
        this.clientRepository = clientRepository;
        
        this.workTypeRefRepository = workTypeRefRepository;
        this.workFamilyRefRepository = workFamilyRefRepository;
        this.statusRefRepository = statusRefRepository;
        
        this.orderRepository = orderRepository;
        
        this.crownWorkService = crownWorkService;
        
        this.fullWorkAssembler = fullWorkAssembler;
        this.workAssembler = workAssembler;
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
    @Transactional(readOnly = true)
    public List<WorkModel> getWorksByOrderId(Long orderId) {
        
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Order not found: " + orderId);
        }

        List<WorkEntity> works = workRepository.findAllByOrderId(orderId);

        return works.stream()
                    .map(workAssembler::toModel)
                    .toList();
    }

    /**
     * Creates a Work (base + extension) with full polymorphic handling.
     */
    @Override
    @Transactional
    public FullWorkModel create(FullWorkModel payload) {

        log.info("Creating new work. Base type: {}", 
                payload.getBase() != null ? payload.getBase().getType() : "null");

        if (payload.getBase() == null) {
            throw new IllegalArgumentException("Work base cannot be null");
        }

        WorkModel baseModel = payload.getBase();

        // ---------------------------------------------------------
        // 1) Validate client
        // ---------------------------------------------------------
        ClientEntity client = clientRepository.findById(baseModel.getClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found: " + baseModel.getClientId()));

        // ---------------------------------------------------------
        // 2) Resolve or create WORK ORDER
        // ---------------------------------------------------------
        WorkOrderEntity order;

        if (baseModel.getOrderId() != null) {
            // order provided → validate it exists
            order = orderRepository.findById(baseModel.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Order not found: " + baseModel.getOrderId()));

            // safety check: order must belong to same client
            if (!Objects.equals(order.getClient().getId(), client.getId())) {
                log.error("Order {} does not belong to client {}", 
                          order.getId(), client.getId());
                throw new IllegalArgumentException("Order does not belong to the client");
            }

        } else {
            // No order → create new order automatically
            log.info("No orderId provided. Creating new order for client {}…", client.getId());

            order = new WorkOrderEntity();
            order.setClient(client);
            order.setDateReceived(LocalDateTime.now());
            order.setStatus("RECEIVED");

            order = orderRepository.save(order);

            log.info("New order created: orderId={}", order.getId());
        }

        // ---------------------------------------------------------
        // 3) Create base WorkEntity
        // ---------------------------------------------------------
        WorkEntity work = new WorkEntity();

        workAssembler.updateEntityFromModel(baseModel, work);

        // REQUIRED: assign FK client + order
        work.setClient(client);
        work.setOrder(order);

        // REQUIRED: assign type lookup
        WorkTypeRefEntity typeRef = workTypeRefRepository
                .findByCode(baseModel.getType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid type: " + baseModel.getType()));

        work.setType(typeRef);

        // REQUIRED: assign family lookup
        WorkFamilyRefEntity familyRef = workFamilyRefRepository
                .findByCode(baseModel.getWorkFamily())
                .orElseThrow(() -> new IllegalArgumentException("Invalid family: " + baseModel.getWorkFamily()));

        work.setWorkFamily(familyRef);
        
        WorkStatusRefEntity statusRef;
        if(baseModel.getStatus() != null) {
	        statusRef = statusRefRepository
					.findByCode(baseModel.getStatus())
					.orElseThrow(() -> new IllegalArgumentException("Invalid status: " + baseModel.getStatus()));
        } else {
			// default status if not provided
			statusRef = statusRefRepository
					.findBySequenceOrder(1)
					.orElseThrow(() -> new IllegalArgumentException("Invalid default status: RECEIVED"));
		}
        
		work.setStatus(statusRef);
        
        // ---------------------------------------------------------
        // 4) Create internalCode and add it to work
        // ---------------------------------------------------------
        addInternalCodeToWork(work, client);

        work = workRepository.save(work);
        log.info("Base work created. workId={} internalCode={} orderId={}",
                work.getId(), work.getInternalCode(), order.getId());

        // ---------------------------------------------------------
        // 5) Create extension entity (CROWN / BRIDGE)
        // ---------------------------------------------------------
        WorkExtensionModel ext = payload.getExtension();

        if (ext != null) {

            ext.setWorkId(work.getId()); // required for conversion

            switch (ext.getType()) {

                case "CROWN" -> {
                    CrownWorkModel model = (CrownWorkModel) ext;
                    CrownWorkEntity extEntity =
                            crownWorkService.toEntity(model, work);
                    work.setCrownWork(extEntity);
                }

                case "BRIDGE" -> {
                    BridgeWorkModel model = (BridgeWorkModel) ext;
                    BridgeWorkEntity extEntity =
                            BridgeWorkAssembler.toEntity(model, work);
                    work.setBridgeWork(extEntity);
                }

                default -> throw new IllegalArgumentException(
                        "Unsupported extension type: " + ext.getType());
            }
            
            log.info("Full work created. extension type={}",
                    ext.getType());
        }

        // ---------------------------------------------------------
        // 6) Return full work model with client + order + extension
        // ---------------------------------------------------------
        return fullWorkAssembler.toModel(work);
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
    
    @Transactional
    private void addInternalCodeToWork(WorkEntity work, ClientEntity client) {

        int year = LocalDate.now().getYear();

        // 1. Detect client profile & prefix (D / S / T)
        String prefix;
        Long profileId;

        if (client.getDentistProfile() != null) {
            prefix = "D";
            profileId = client.getDentistProfile().getId();
        } else if (client.getStudentProfile() != null) {
            prefix = "S";
            profileId = client.getStudentProfile().getId();
        } else if (client.getTechnicianProfile() != null) {
            prefix = "T";
            profileId = client.getTechnicianProfile().getId();
        } else {
            throw new IllegalStateException("Client without profile should not exist.");
        }

        // 2. Get previous max seq for this profile & year
        Integer maxSeq = workRepository
                .findMaxSeqForProfileAndYear(profileId, year)
                .orElse(0);

        int nextSeq = maxSeq + 1;

        // 3. Assign metadata
        work.setProfilePrefix(prefix);
        work.setClientProfileId(profileId);
        work.setInternalSeq(nextSeq);
        work.setInternalYear(year);

        // 4. Generate human-readable code
        String internalCode = String.format(
                "%s%d-%03d-%02d",
                prefix,
                profileId,
                nextSeq,
                year % 100
        );

        work.setInternalCode(internalCode);

        // At this point work.getId() is null (not saved yet)
        log.info("Generated internal code '{}' (profile={} seq={} year={})",
                internalCode, profileId, nextSeq, year);
    }


}
