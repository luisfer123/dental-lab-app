package com.dentallab.persistence.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;

public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, Long> {

    /**
     * Find all orders for a specific client.
     */
    Page<WorkOrderEntity> findByClient(ClientEntity client, Pageable pageable);

    /**
     * Find orders by status (e.g., RECEIVED, ASSIGNED, FINISHED, DELIVERED).
     */
    Page<WorkOrderEntity> findByStatus(String status, Pageable pageable);

    /**
     * Find all orders received between two timestamps.
     */
    Page<WorkOrderEntity> findByDateReceivedBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<WorkOrderEntity> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find overdue (not delivered) orders.
     */
    @Query("""
           SELECT o
           FROM WorkOrderEntity o
           WHERE o.dueDate < :now
             AND o.deliveredAt IS NULL
           """)
    Page<WorkOrderEntity> findOverdueOrders(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Orders that should be delivered today (dueDate = today).
     */
    @Query("""
           SELECT o
           FROM WorkOrderEntity o
           WHERE DATE(o.dueDate) = CURRENT_DATE
           """)
    Page<WorkOrderEntity> findDueToday(Pageable pageable);
}
