package com.dentallab.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.WorkEntity;

@Repository
public interface WorkRepository
        extends JpaRepository<WorkEntity, Long>,
                JpaSpecificationExecutor<WorkEntity> {

    // ---------------------------------------------------------
    //  LOOKUP-SAFE METHODS
    // ---------------------------------------------------------

    /**
     * Checks if a work exists by id and work family code.
     * Example usage:
     * existsByIdAndWorkFamily_Code(10L, "FIXED_PROSTHESIS")
     */
    boolean existsByIdAndWorkFamily_Code(Long id, String familyCode);

    /**
     * Checks if a work exists by id and type code.
     * Example usage:
     * existsByIdAndType_Code(10L, "CROWN")
     */
    boolean existsByIdAndType_Code(Long id, String typeCode);

    /**
     * Count works by type code.
     * Example usage:
     * countByType_Code("BRIDGE")
     */
    long countByType_Code(String typeCode);

    // ---------------------------------------------------------
    // OPTIONAL FILTER METHODS (recommended)
    // ---------------------------------------------------------
    /**
     * Get all works for a family.
     */
    List<WorkEntity> findByWorkFamily_Code(String familyCode);

    /**
     * Get all works for a type.
     */
    List<WorkEntity> findByType_Code(String typeCode);

    /**
     * Filter by workflow status (work.status column).
     * This one stays a simple string because work.status is not an entity.
     */
    List<WorkEntity> findByStatus_Code(String statusCode);
    
    /**
     * Page only Work IDs using Specifications.
     */
    @Query("SELECT w.id FROM WorkEntity w")
    Page<Long> findIds(Pageable pageable);

    /**
     * Load Work + Client using join fetch.
     */
    @Query("""
        SELECT w
        FROM WorkEntity w
        LEFT JOIN FETCH w.client
        WHERE w.id IN :ids
        """)
    List<WorkEntity> findAllWithClientByIdIn(List<Long> ids);
    
    @Query("""
    	    SELECT MAX(w.internalSeq)
    	    FROM WorkEntity w
    	    WHERE w.clientProfileId = :profileId
    	      AND w.internalYear = :year
    """)
	Optional<Integer> findMaxSeqForProfileAndYear(
	        @Param("profileId") Long profileId,
	        @Param("year") Integer year
	);

    @Query("SELECT w FROM WorkEntity w WHERE w.order.id = :orderId")
    List<WorkEntity> findAllByOrderId(Long orderId);
    
    // Excludes works without a price. To include those use LEFT JOIN w.price wp.
    @Query("""
            SELECT w
				FROM WorkEntity w
				JOIN w.price wp
				WHERE w.client.id = :clientId
				  AND wp.price >
				      COALESCE(
				          (SELECT SUM(pa.amountApplied)
				           FROM PaymentAllocationEntity pa
				           WHERE pa.workId = w.id), 0
				      )
				      +
				      COALESCE(
				          (SELECT SUM(ABS(cbm.amountChange))
				           FROM ClientBalanceMovementEntity cbm
				           WHERE cbm.workId = w.id
				             AND cbm.type = 'APPLY_WORK'), 0
				      )
				ORDER BY w.createdAt ASC
        """)
        List<WorkEntity> findUnpaidWorksByClientId(@Param("clientId") Long clientId);
    
    /**
     * Loads works by ids ensuring they belong to the given client.
     * Used by payment preview to prevent cross-client allocations.
     */
    List<WorkEntity> findByIdInAndClient_Id(List<Long> ids, Long clientId);


}
