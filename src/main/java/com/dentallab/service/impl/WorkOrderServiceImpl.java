package com.dentallab.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.assembler.FullWorkOrderAssembler;
import com.dentallab.api.assembler.WorkOrderAssembler;
import com.dentallab.api.model.FullWorkOrderModel;
import com.dentallab.api.model.WorkOrderModel;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;
import com.dentallab.persistence.repository.ClientRepository;
import com.dentallab.persistence.repository.WorkOrderRepository;
import com.dentallab.service.WorkOrderService;

@Service
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderServiceImpl.class);

    private final WorkOrderRepository orderRepository;
    private final ClientRepository clientRepository;

    private final WorkOrderAssembler orderAssembler;
    private final FullWorkOrderAssembler fullOrderAssembler;

    public WorkOrderServiceImpl(
            WorkOrderRepository orderRepository,
            ClientRepository clientRepository,
            WorkOrderAssembler orderAssembler,
            FullWorkOrderAssembler fullOrderAssembler
    ) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.orderAssembler = orderAssembler;
        this.fullOrderAssembler = fullOrderAssembler;
    }

    /* ============================================================
       Pagination helper
    ============================================================ */
    private Pageable buildPage(int page, int size, String sort) {

        log.debug("Building pageable: page={}, size={}, sort={}", page, size, sort);

        Sort s = Sort.by("createdAt").descending();

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            String dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                         ? "asc" : "desc";

            s = dir.equals("asc")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        }

        return PageRequest.of(page, size, s);
    }

    /* ============================================================
       ENTITY <-> MODEL converters
    ============================================================ */
    private WorkOrderEntity toEntity(WorkOrderModel model, WorkOrderEntity entity) {

        log.debug("Converting WorkOrderModel → WorkOrderEntity (before): {}", entity.getId());

        if (model.getDateReceived() != null) {
            log.debug("Setting dateReceived={}", model.getDateReceived());
            entity.setDateReceived(model.getDateReceived());
        }

        if (model.getDueDate() != null) {
            log.debug("Setting dueDate={}", model.getDueDate());
            entity.setDueDate(model.getDueDate());
        }

        if (model.getDeliveredAt() != null) {
            log.debug("Setting deliveredAt={}", model.getDeliveredAt());
            entity.setDeliveredAt(model.getDeliveredAt());
        }

        if (model.getStatus() != null) {
            log.debug("Setting status={}", model.getStatus());
            entity.setStatus(model.getStatus());
        }

        if (model.getNotes() != null) {
            log.debug("Setting notes={}", model.getNotes());
            entity.setNotes(model.getNotes());
        }

        return entity;
    }

    /* ============================================================
       CREATE (from model)
    ============================================================ */
    @Override
    public FullWorkOrderModel create(WorkOrderModel model) {

        log.info("Creating new WorkOrder for clientId={}", model.getClientId());

        ClientEntity client = clientRepository.findById(model.getClientId())
                .orElseThrow(() -> {
                    log.error("Client not found: {}", model.getClientId());
                    return new IllegalArgumentException("Client not found: " + model.getClientId());
                });

        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setClient(client);

        toEntity(model, entity);

        WorkOrderEntity saved = orderRepository.save(entity);

        log.info("WorkOrder created successfully: orderId={}", saved.getId());

        return fullOrderAssembler.toModel(saved);
    }

    /* ============================================================
       CREATE order for a client (Angular workflow)
    ============================================================ */
    @Override
    public FullWorkOrderModel createOrderForClient(Long clientId) {

        log.info("Creating WorkOrder for clientId={} (from wizard flow)", clientId);

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.error("Client not found: {}", clientId);
                    return new IllegalArgumentException("Client not found: " + clientId);
                });

        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setClient(client);
        entity.setDateReceived(LocalDateTime.now());
        entity.setStatus("RECEIVED");

        WorkOrderEntity saved = orderRepository.save(entity);

        log.info("WorkOrder created for client {} → orderId={}", clientId, saved.getId());

        return fullOrderAssembler.toModel(saved);
    }

    /* ============================================================
       GET (single)
    ============================================================ */
    @Override
    @Transactional(readOnly = true)
    public FullWorkOrderModel getById(Long id) {
        log.debug("Fetching WorkOrder by id={}", id);

        WorkOrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkOrder not found: {}", id);
                    return new IllegalArgumentException("Order not found: " + id);
                });

        return fullOrderAssembler.toModel(entity);
    }

    /* ============================================================
       GET (paginated)
    ============================================================ */
    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderModel> getAll(int page, int size, String sort) {

        log.debug("Fetching all WorkOrders paginated: page={}, size={}, sort={}", page, size, sort);

        Pageable pageable = buildPage(page, size, sort);

        return orderRepository.findAll(pageable)
                .map(orderAssembler::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderModel> getByClientId(Long clientId, int page, int size, String sort) {

        log.debug("Fetching WorkOrders for clientId={}, page={}, size={}", clientId, page, size);

        Pageable pageable = buildPage(page, size, sort);

        return orderRepository.findByClientId(clientId, pageable)
                .map(orderAssembler::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderModel> getOverdueOrders(int page, int size, String sort) {

        log.warn("Fetching OVERDUE WorkOrders... page={}, size={}", page, size);

        Pageable pageable = buildPage(page, size, sort);

        return orderRepository.findOverdueOrders(LocalDateTime.now(), pageable)
                .map(orderAssembler::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderModel> getDueToday(int page, int size, String sort) {

        log.debug("Fetching WorkOrders due TODAY: page={}, size={}", page, size);

        Pageable pageable = buildPage(page, size, sort);

        return orderRepository.findDueToday(pageable)
                .map(orderAssembler::toModel);
    }

    /* ============================================================
       UPDATE
    ============================================================ */
    @Override
    public FullWorkOrderModel update(Long id, WorkOrderModel model) {

        log.info("Updating WorkOrder id={}", id);

        WorkOrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkOrder not found for update: {}", id);
                    return new IllegalArgumentException("Order not found: " + id);
                });

        toEntity(model, entity);

        WorkOrderEntity saved = orderRepository.save(entity);

        log.info("WorkOrder updated successfully: {}", id);

        return fullOrderAssembler.toModel(saved);
    }

    /* ============================================================
       MARK DELIVERED
    ============================================================ */
    @Override
    public FullWorkOrderModel markDelivered(Long id) {

        log.info("Marking WorkOrder as delivered: {}", id);

        WorkOrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkOrder not found for markDelivered: {}", id);
                    return new IllegalArgumentException("Order not found: " + id);
                });

        entity.setDeliveredAt(LocalDateTime.now());
        entity.setStatus("DELIVERED");

        WorkOrderEntity saved = orderRepository.save(entity);

        log.info("WorkOrder {} marked as DELIVERED", id);

        return fullOrderAssembler.toModel(saved);
    }

    /* ============================================================
       DELETE
    ============================================================ */
    @Override
    public void delete(Long id) {

        log.warn("Deleting WorkOrder id={}", id);

        WorkOrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkOrder not found for delete: {}", id);
                    return new IllegalArgumentException("Order not found: " + id);
                });

        orderRepository.delete(entity);

        log.info("WorkOrder deleted successfully: {}", id);
    }
}
