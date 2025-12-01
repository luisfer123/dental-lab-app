package com.dentallab.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dentallab.persistence.entity.MaterialEntity;

public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

}
