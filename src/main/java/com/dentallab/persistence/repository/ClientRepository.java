package com.dentallab.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.ClientEntity;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

	boolean existsById(Long clientId);

	boolean existsByPrimaryEmail(String email);
	
	@Query("""
	       SELECT c FROM ClientEntity c
	       WHERE LOWER(c.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
	          OR LOWER(c.primaryEmail) LIKE LOWER(CONCAT('%', :query, '%'))
	          OR LOWER(c.primaryPhone) LIKE LOWER(CONCAT('%', :query, '%'))
	       """)
	Page<ClientEntity> searchByNameEmailPhone(@Param("query") String query, Pageable pageable);

	// =============================
	// CLIENT REPOSITORY EXTENSIONS
	// =============================

	@Query("""
	    SELECT c
	    FROM ClientEntity c
	    JOIN c.dentistProfile d
	""")
	Page<ClientEntity> findAllWithDentistProfile(Pageable pageable);

	@Query("""
	    SELECT c
	    FROM ClientEntity c
	    JOIN c.studentProfile s
	""")
	Page<ClientEntity> findAllWithStudentProfile(Pageable pageable);

	@Query("""
	    SELECT c
	    FROM ClientEntity c
	    JOIN c.technicianProfile t
	""")
	Page<ClientEntity> findAllWithTechnicianProfile(Pageable pageable);


}
