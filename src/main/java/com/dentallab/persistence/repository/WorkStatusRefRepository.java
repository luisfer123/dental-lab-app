package com.dentallab.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dentallab.persistence.entity.WorkStatusRefEntity;

public interface WorkStatusRefRepository extends JpaRepository<WorkStatusRefEntity, String> {

    List<WorkStatusRefEntity> findAllByOrderBySequenceOrderAsc();

    WorkStatusRefEntity findBySequenceOrder(Integer order);

    boolean existsBySequenceOrder(Integer order);
}
