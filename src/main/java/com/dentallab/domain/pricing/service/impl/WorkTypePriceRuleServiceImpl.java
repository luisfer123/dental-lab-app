package com.dentallab.domain.pricing.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.pricing.exception.DuplicateWorkTypePriceRuleException;
import com.dentallab.domain.pricing.model.CreateWorkTypePriceRuleCommand;
import com.dentallab.domain.pricing.service.WorkTypePriceRuleService;
import com.dentallab.persistence.entity.WorkTypePriceEntity;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkTypePriceRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;

@Service
@Transactional
public class WorkTypePriceRuleServiceImpl implements WorkTypePriceRuleService {

    private static final Logger log = LoggerFactory.getLogger(WorkTypePriceRuleServiceImpl.class);

    private final WorkTypePriceRepository workTypePriceRepository;
    private final WorkFamilyRefRepository workFamilyRefRepository;
    private final WorkTypeRefRepository workTypeRefRepository;

    public WorkTypePriceRuleServiceImpl(
            WorkTypePriceRepository workTypePriceRepository,
            WorkFamilyRefRepository workFamilyRefRepository,
            WorkTypeRefRepository workTypeRefRepository
    ) {
        this.workTypePriceRepository = workTypePriceRepository;
        this.workFamilyRefRepository = workFamilyRefRepository;
        this.workTypeRefRepository = workTypeRefRepository;
    }

    @Override
    public WorkTypePriceEntity createRule(CreateWorkTypePriceRuleCommand cmd) {

        Objects.requireNonNull(cmd, "command must not be null");

        String family = cmd.getWorkFamily();
        String type   = cmd.getWorkType();

        log.info(
            "Creating work_type_price rule: family={}, type={}, group={}, validFrom={}",
            family, type, cmd.getPriceGroup(), cmd.getValidFrom()
        );

        // ---------------------------------------------------------------------
        // Reference validation
        // ---------------------------------------------------------------------
        if (!workFamilyRefRepository.existsById(family)) {
            throw new IllegalArgumentException("Unknown workFamily code: " + family);
        }
        if (!workTypeRefRepository.existsById(type)) {
            throw new IllegalArgumentException("Unknown workType code: " + type);
        }

        // ---------------------------------------------------------------------
        // Semantic duplicate check (PRIMARY GUARD)
        // ---------------------------------------------------------------------
        boolean duplicateExists =
            workTypePriceRepository.existsExactRule(
                family,
                type,
                cmd.getPriceGroup(),
                cmd.getConstitution(),
                cmd.getBuildingTechnique() == null ? null : cmd.getBuildingTechnique().name(),
                cmd.getCoreMaterialId(),
                cmd.getDentureVariant(),
                cmd.getResinType(),
                cmd.getTeethType(),
                cmd.getValidFrom()
            );

        if (duplicateExists) {
            log.warn(
                "Duplicate work_type_price rule rejected: family={}, type={}, group={}, validFrom={}",
                family, type, cmd.getPriceGroup(), cmd.getValidFrom()
            );
            throw new DuplicateWorkTypePriceRuleException(
                "A pricing rule already exists for the same attributes and validFrom"
            );
        }

        // ---------------------------------------------------------------------
        // Persistence
        // ---------------------------------------------------------------------
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setWorkFamily(family);
        rule.setWorkType(type);
        rule.setPriceGroup(cmd.getPriceGroup());
        rule.setConstitution(cmd.getConstitution());
        rule.setBuildingTechnique(
            cmd.getBuildingTechnique() == null ? null : cmd.getBuildingTechnique().name()
        );
        rule.setCoreMaterialId(cmd.getCoreMaterialId());
        rule.setDentureVariant(cmd.getDentureVariant());
        rule.setResinType(cmd.getResinType());
        rule.setTeethType(cmd.getTeethType());
        rule.setBasePrice(cmd.getBasePrice());
        rule.setPricePerUnit(cmd.getPricePerUnit());
        rule.setCurrency(cmd.getCurrency());
        rule.setValidFrom(cmd.getValidFrom());
        rule.setNotes(cmd.getNotes());

        try {
            WorkTypePriceEntity saved = workTypePriceRepository.save(rule);

            log.info(
                "work_type_price rule created: priceId={}, family={}, type={}, group={}, validFrom={}",
                saved.getPriceId(), family, type, cmd.getPriceGroup(), cmd.getValidFrom()
            );

            return saved;

        } catch (DataIntegrityViolationException ex) {
            // SECONDARY GUARD (DB safety net)
            throw new DuplicateWorkTypePriceRuleException(
                "A pricing rule already exists for the same attributes and validFrom",
                ex
            );
        }
    }

}
