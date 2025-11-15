package com.dentallab.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.dentallab.persistence.entity.WorkTypeRefEntity;

public interface WorkTypeRefRepository extends JpaRepository<WorkTypeRefEntity, String> {

    List<WorkTypeRefEntity> findByFamilyCode(String familyCode);
}
