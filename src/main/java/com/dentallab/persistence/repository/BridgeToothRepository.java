package com.dentallab.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentallab.persistence.entity.BridgeToothEntity;
import com.dentallab.persistence.entity.BridgeToothId;

public interface BridgeToothRepository
        extends JpaRepository<BridgeToothEntity, BridgeToothId> {

    List<BridgeToothEntity> findByBridgeWork_WorkId(Long bridgeWorkId);

    long countByBridgeWork_WorkId(Long bridgeWorkId);
}
