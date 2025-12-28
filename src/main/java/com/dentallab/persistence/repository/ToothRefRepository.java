package com.dentallab.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dentallab.persistence.entity.ToothRefEntity;

public interface ToothRefRepository
        extends JpaRepository<ToothRefEntity, Long> {
}
