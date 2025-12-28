package com.dentallab.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.dto.BridgeToothRequest;
import com.dentallab.api.dto.BridgeWorkUpsertRequest;
import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;
import com.dentallab.persistence.entity.BridgeToothEntity;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.ToothRefEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.BridgeWorkRepository;
import com.dentallab.persistence.repository.ToothRefRepository;
import com.dentallab.persistence.repository.WorkRepository;
import com.dentallab.service.BridgeWorkService;

/**
 * <h2>BridgeWorkServiceImpl</h2>
 *
 * <p>
 * Domain service responsible for creating and updating
 * <strong>bridge-specific data</strong> associated with a {@link WorkEntity}.
 * </p>
 *
 * <p>
 * This service manages:
 * </p>
 *
 * <ul>
 *   <li>the {@link BridgeWorkEntity} extension table</li>
 *   <li>the normalized bridge tooth structure ({@code bridge_tooth})</li>
 * </ul>
 *
 * <p>
 * Bridge teeth are treated as <strong>structural data</strong>, not pricing data.
 * Any pricing implications (e.g. prosthetic units) are resolved upstream by
 * pricing queries.
 * </p>
 *
 * <h3>Key characteristics</h3>
 *
 * <ul>
 *   <li>Transactional and atomic</li>
 *   <li>Uses {@code @MapsId} (bridge shares PK with work)</li>
 *   <li>Replaces bridge teeth deterministically on update</li>
 *   <li>Fails fast on invalid or inconsistent input</li>
 * </ul>
 */
@Service
@Transactional
public class BridgeWorkServiceImpl implements BridgeWorkService {

    private static final Logger log =
            LoggerFactory.getLogger(BridgeWorkServiceImpl.class);

    private final WorkRepository workRepository;
    private final BridgeWorkRepository bridgeWorkRepository;
    private final ToothRefRepository toothRefRepository;

    public BridgeWorkServiceImpl(
            WorkRepository workRepository,
            BridgeWorkRepository bridgeWorkRepository,
            ToothRefRepository toothRefRepository
    ) {
        this.workRepository = workRepository;
        this.bridgeWorkRepository = bridgeWorkRepository;
        this.toothRefRepository = toothRefRepository;
    }

    /**
     * Creates or updates the bridge-specific data for an existing work.
     *
     * <p>
     * This method performs a full <strong>upsert</strong>:
     * </p>
     *
     * <ul>
     *   <li>Validates bridge teeth structure</li>
     *   <li>Loads or creates {@link BridgeWorkEntity}</li>
     *   <li>Replaces all bridge teeth deterministically</li>
     * </ul>
     *
     * <p>
     * If bridge teeth already exist, they are fully replaced
     * (via {@code orphanRemoval = true}).
     * </p>
     *
     * @param workId the identifier of the parent work
     * @param req    bridge data and normalized teeth
     * @return the persisted {@link BridgeWorkEntity}
     *
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    public BridgeWorkEntity upsertBridgeWork(Long workId, BridgeWorkUpsertRequest req) {

        log.info("Upserting bridge work for workId={}", workId);

        validateBridgeTeeth(req.getTeeth());

        // ----------------------------------------------------------
        // Load parent Work (required for @MapsId)
        // ----------------------------------------------------------
        WorkEntity work = workRepository.findById(workId)
                .orElseThrow(() -> {
                    log.warn("Work not found for bridge upsert: workId={}", workId);
                    return new IllegalArgumentException("Work not found: " + workId);
                });

        // ----------------------------------------------------------
        // Load or create BridgeWorkEntity
        // ----------------------------------------------------------
        BridgeWorkEntity bridge = bridgeWorkRepository.findById(workId)
                .orElseGet(() -> {
                    log.debug("Creating new BridgeWorkEntity for workId={}", workId);
                    return new BridgeWorkEntity(work);
                });

        // ----------------------------------------------------------
        // Update bridge core fields
        // ----------------------------------------------------------
        bridge.setConstitution(FixProstheticConstitution.valueOf(req.getConstitution()));
        bridge.setBuildingTechnique(BuildingTechnique.valueOf(req.getBuildingTechnique()));
        bridge.setCoreMaterialId(req.getCoreMaterialId());
        bridge.setVeneeringMaterialId(req.getVeneeringMaterialId());
        bridge.setConnectorType(req.getConnectorType());
        bridge.setPonticDesign(req.getPonticDesign());
        bridge.setNotes(req.getNotes());

        // ----------------------------------------------------------
        // Replace bridge teeth
        // ----------------------------------------------------------
        replaceBridgeTeeth(bridge, req.getTeeth());

        BridgeWorkEntity saved = bridgeWorkRepository.save(bridge);

        log.info(
                "Bridge work upserted successfully: workId={}, teethCount={}",
                workId,
                saved.getBridgeTeeth().size()
        );

        return saved;
    }

    // ==========================================================
    // Validation
    // ==========================================================

    /**
     * Validates structural invariants for bridge teeth.
     *
     * <p>
     * Enforces:
     * </p>
     *
     * <ul>
     *   <li>no duplicate teeth</li>
     *   <li>valid tooth roles</li>
     *   <li>at least two abutments</li>
     *   <li>at least one pontic</li>
     *   <li>valid and unique position indexes</li>
     * </ul>
     *
     * @param teeth normalized bridge teeth requests
     */
    private void validateBridgeTeeth(List<BridgeToothRequest> teeth) {

        if (teeth == null || teeth.isEmpty()) {
            throw new IllegalArgumentException("Bridge teeth list is required.");
        }

        Set<Long> seenToothIds = new HashSet<>();
        Set<Integer> seenPositions = new HashSet<>();

        int abutments = 0;
        int pontics = 0;

        for (BridgeToothRequest t : teeth) {

            if (t.getToothId() == null) {
                throw new IllegalArgumentException("toothId is required for each tooth.");
            }

            if (!seenToothIds.add(t.getToothId())) {
                throw new IllegalArgumentException(
                        "Duplicate toothId in bridge: " + t.getToothId()
                );
            }

            BridgeToothEntity.Role role;
            try {
                role = BridgeToothEntity.Role.valueOf(t.getRole());
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid tooth role '" + t.getRole()
                        + "'. Allowed values: ABUTMENT, PONTIC"
                );
            }

            if (role == BridgeToothEntity.Role.ABUTMENT) abutments++;
            if (role == BridgeToothEntity.Role.PONTIC) pontics++;

            if (t.getPositionIndex() != null) {
                if (t.getPositionIndex() < 1) {
                    throw new IllegalArgumentException("positionIndex must be >= 1.");
                }
                if (!seenPositions.add(t.getPositionIndex())) {
                    throw new IllegalArgumentException(
                            "Duplicate positionIndex: " + t.getPositionIndex()
                    );
                }
            }
        }

        if (abutments < 2) {
            throw new IllegalArgumentException(
                    "Bridge must have at least 2 abutment teeth."
            );
        }

        if (pontics < 1) {
            throw new IllegalArgumentException(
                    "Bridge must have at least 1 pontic tooth."
            );
        }
    }

    // ==========================================================
    // Bridge tooth replacement
    // ==========================================================

    /**
     * Replaces all bridge teeth associated with a bridge work.
     *
     * <p>
     * Existing teeth are removed via {@code orphanRemoval = true}.
     * New teeth are attached deterministically.
     * </p>
     *
     * @param bridge   the owning bridge work
     * @param teethReq normalized tooth requests
     */
    private void replaceBridgeTeeth(
            BridgeWorkEntity bridge,
            List<BridgeToothRequest> teethReq
    ) {
        log.debug(
                "Replacing bridge teeth for workId={}, newCount={}",
                bridge.getId(),
                teethReq.size()
        );

        bridge.getBridgeTeeth().clear();

        Map<Long, ToothRefEntity> toothMap = loadToothRefs(teethReq);

        for (BridgeToothRequest t : teethReq) {

            ToothRefEntity tooth = toothMap.get(t.getToothId());

            BridgeToothEntity.Role role =
                    BridgeToothEntity.Role.valueOf(t.getRole());

            BridgeToothEntity bridgeTooth = new BridgeToothEntity(
                    bridge,
                    tooth,
                    role,
                    t.getPositionIndex()
            );

            bridge.getBridgeTeeth().add(bridgeTooth);
        }
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    /**
     * Loads {@link ToothRefEntity} records required by the bridge,
     * validating that all referenced teeth exist.
     *
     * @param teethReq normalized tooth requests
     * @return map of toothId → ToothRefEntity
     */
    private Map<Long, ToothRefEntity> loadToothRefs(
            List<BridgeToothRequest> teethReq
    ) {
        Set<Long> ids = new HashSet<>();
        for (BridgeToothRequest t : teethReq) {
            ids.add(t.getToothId());
        }

        List<ToothRefEntity> found = toothRefRepository.findAllById(ids);

        Map<Long, ToothRefEntity> map = new HashMap<>();
        for (ToothRefEntity tr : found) {
            map.put(tr.getId(), tr);
        }

        for (Long id : ids) {
            if (!map.containsKey(id)) {
                throw new IllegalArgumentException(
                        "toothId not found in tooth_ref: " + id
                );
            }
        }

        return map;
    }
}
