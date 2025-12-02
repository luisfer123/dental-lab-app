package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a work order in the dental lab system.
 * Mirrors the structure of the work_order table.
 */
@Entity
@Table(name = "work_order")
public class WorkOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "client_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_workorder_client")
    )
    private ClientEntity client;

    @Column(name = "date_received", nullable = false)
    private LocalDateTime dateReceived;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<WorkEntity> works;

    /* ============================================================
       Constructors
    ============================================================ */

    public WorkOrderEntity() {
    }

    public WorkOrderEntity(Long id) {
        this.id = id;
    }

    public WorkOrderEntity(
            Long id,
            ClientEntity client,
            LocalDateTime dateReceived,
            LocalDateTime dueDate,
            LocalDateTime deliveredAt,
            String status,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<WorkEntity> works
    ) {
        this.id = id;
        this.client = client;
        this.dateReceived = dateReceived;
        this.dueDate = dueDate;
        this.deliveredAt = deliveredAt;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.works = works;
    }

    /* ============================================================
       Getters and Setters
    ============================================================ */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClientEntity getClient() {
        return client;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }

    public LocalDateTime getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(LocalDateTime dateReceived) {
        this.dateReceived = dateReceived;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<WorkEntity> getWorks() {
        return works;
    }

    public void setWorks(List<WorkEntity> works) {
        this.works = works;
    }

    /* ============================================================
       Lifecycle callbacks
    ============================================================ */

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.dateReceived == null) {
            this.dateReceived = now;
        }

        if (this.status == null) {
            this.status = "RECEIVED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ============================================================
       equals and hashCode (ONLY ID) — Correct JPA practice
    ============================================================ */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkOrderEntity)) return false;
        WorkOrderEntity that = (WorkOrderEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    /* ============================================================
       toString (No recursion → do NOT include works)
    ============================================================ */

    @Override
    public String toString() {
        return "WorkOrderEntity{" +
                "id=" + id +
                ", clientId=" + (client != null ? client.getId() : null) +
                ", dateReceived=" + dateReceived +
                ", dueDate=" + dueDate +
                ", deliveredAt=" + deliveredAt +
                ", status='" + status + '\'' +
                ", notes='" + (notes != null ? notes.substring(0, Math.min(notes.length(), 30)) + "..." : null) + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

/**
 * ============================================================
 *                 WORK ORDER → WORKS WORKFLOW
 * ============================================================
 *
 *                 +---------------------------+
 *                 |        WORK ORDER         |
 *                 |---------------------------|
 *   Creates at →  |  created_at               |
 *   Received at → |  date_received            |
 *   Due at →      |  due_date                 |
 *   Delivered at →|  delivered_at             |
 *   Status →      |  RECEIVED / ASSIGNED / ...|
 *                 +-------------+-------------+
 *                               |
 *                     1 : N     |
 *                               v
 *                 +---------------------------+
 *                 |            WORK           |
 *                 |---------------------------|
 *                 |  work_family / type       |
 *                 |  status (general)         |
 *                 +-------------+-------------+
 *                               |
 *                     1 : 1     |  (based on type)
 *                 -----------------------------
 *                 |            |              |
 *                 v            v              v
 *       +-------------+  +------------+  +------------+
 *       | CROWN WORK  |  | BRIDGE WORK|  | INLAY WORK |
 *       +-------------+  +------------+  +------------+
 *           |                |               |
 *           +-------+--------+-------+-------+
 *                            |
 *                            v
 *              +------------------------------+
 *       	    |   BUILDING STATUS (tech)    |
 *       		|  SCAN → DESIGN → MILL → ... |
 *     		  	+------------------------------+
 *
 *  Notes:
 *  - WorkOrder groups multiple Work items.
 *  - date_received = physical arrival to the lab.
 *  - created_at = when the order is registered in system.
 *  - due_date = promised delivery date/time.
 *  - delivered_at = actual delivery timestamp.
 *  - status is validated against work_status_ref.
 *  - Each Work may have its own assigned technician and
 *    technical building-status progression.
 *
 * ============================================================
 */

