package com.dentallab.service.impl;

import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.persistence.entity.WorkStatusRefEntity;
import com.dentallab.persistence.repository.WorkStatusRefRepository;
import com.dentallab.service.WorkStatusRefService;

/**
 * Service responsible for managing the linear workflow status chain.
 *
 * Responsibilities:
 *  - Load all statuses in sequence order
 *  - Insert a new status before or after an existing one
 *  - Shift sequenceOrder values automatically
 *  - Enforce linear chain consistency
 *
 * Notes:
 *  - sequenceOrder is a continuous list: 1,2,3,4,...
 *  - When inserting a new status, the service shifts other entries accordingly
 *  - This service should NOT be used inside assemblers or lookup helpers
 */
@Service
@Transactional
public class WorkStatusRefServiceImpl implements WorkStatusRefService {

    private static final Logger log = LoggerFactory.getLogger(WorkStatusRefServiceImpl.class);

    private final WorkStatusRefRepository statusRepo;

    public WorkStatusRefServiceImpl(WorkStatusRefRepository statusRepo) {
        this.statusRepo = statusRepo;
    }

    // ================================================================
    // GET ALL
    // ================================================================
    @Override
    @Transactional(readOnly = true)
    public List<WorkStatusRefEntity> getAllOrdered() {
        log.debug("Fetching all work statuses ordered by sequenceOrder");
        return statusRepo.findAllByOrderBySequenceOrderAsc();
    }

    // ================================================================
    // INSERT AFTER
    // ================================================================
    @Override
    public WorkStatusRefEntity insertAfter(String newCode, String newLabel, String afterCode) {

        log.info("Inserting new status '{}' after '{}'", newCode, afterCode);

        WorkStatusRefEntity ref = statusRepo.findById(afterCode)
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + afterCode));

        int insertPos = ref.getSequenceOrder() + 1;
        shiftDownFrom(insertPos);

        WorkStatusRefEntity newEntity = new WorkStatusRefEntity();
        newEntity.setCode(newCode);
        newEntity.setLabel(newLabel);
        newEntity.setSequenceOrder(insertPos);

        statusRepo.save(newEntity);

        log.info("Inserted status '{}' at position {}", newCode, insertPos);

        return newEntity;
    }

    // ================================================================
    // INSERT BEFORE
    // ================================================================
    @Override
    /**
     * Inserts a new status before the specified existing status.
     * 
     * @param newCode   The code of the new status to insert.
     * @param newLabel  The label of the new status to insert.
     * @param beforeCode The code of the existing status before which to insert the new status.
     * @return The newly inserted WorkStatusRefEntity.
     * @throws IllegalArgumentException if the beforeCode does not exist.
     */
    public WorkStatusRefEntity insertBefore(String newCode, String newLabel, String beforeCode) {

        log.info("Inserting new status '{}' before '{}'", newCode, beforeCode);

        WorkStatusRefEntity ref = statusRepo.findById(beforeCode)
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + beforeCode));

        int insertPos = ref.getSequenceOrder();
        shiftDownFrom(insertPos);

        WorkStatusRefEntity newEntity = new WorkStatusRefEntity();
        newEntity.setCode(newCode);
        newEntity.setLabel(newLabel);
        newEntity.setSequenceOrder(insertPos);

        statusRepo.save(newEntity);

        log.info("Inserted status '{}' at position {}", newCode, insertPos);

        return newEntity;
    }

    // ================================================================
    // REORDER (manual full reorder)
    // ================================================================
    /**
     * Reorders the full list of statuses according to the provided list of codes.
     * 
     * @param orderedCodes The list of status codes in the desired order.
     * @throws IllegalArgumentException if the provided list does not match the existing statuses.
     */
    @Override
    public void reorder(List<String> orderedCodes) {

        log.warn("Reordering full workflow statuses ({} entries)", orderedCodes.size());

        List<WorkStatusRefEntity> all = statusRepo.findAll();

        if (all.size() != orderedCodes.size()) {
            throw new IllegalArgumentException("Reorder mismatch: full list must be provided");
        }

        // Apply new order
        IntStream.range(0, orderedCodes.size()).forEach(i -> {
            String code = orderedCodes.get(i);
            WorkStatusRefEntity st = all.stream()
                    .filter(s -> s.getCode().equals(code))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Unknown status code in reorder: " + code));

            st.setSequenceOrder(i + 1);
            statusRepo.save(st);
        });

        log.info("Status reorder completed successfully");
    }

    // ================================================================
    // INTERNAL UTILS
    // ================================================================
    /**
     * Shifts all statuses with sequenceOrder >= start
     * so they move down by 1 (used when inserting).
     * 
     * @param start The sequenceOrder from which to start shifting.
     */
    private void shiftDownFrom(int start) {
        log.debug("Shifting status sequence: all with sequenceOrder >= {} move down by 1", start);

        List<WorkStatusRefEntity> items = statusRepo.findAllByOrderBySequenceOrderAsc();

        for (WorkStatusRefEntity s : items) {
            if (s.getSequenceOrder() >= start) {
                s.setSequenceOrder(s.getSequenceOrder() + 1);
                statusRepo.save(s);
            }
        }
    }
}
