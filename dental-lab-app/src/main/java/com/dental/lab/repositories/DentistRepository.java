package com.dental.lab.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dental.lab.model.entities.Dentist;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
	
	boolean existsByUserId(Long id);

}
