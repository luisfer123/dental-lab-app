package com.dentallab.api.model;

import java.time.LocalDateTime;
import org.springframework.hateoas.RepresentationModel;

/**
 * Base HATEOAS model for a general work item.
 * Represents a single dental work (e.g., crown, bridge, inlay)
 * with lookup-based family/type codes and human-readable labels.
 */
public class WorkModel extends RepresentationModel<WorkModel> {

    private Long id;

    /** Code from work_type_ref (e.g., "CROWN", "BRIDGE") */
    private String type;

    /** Code from work_family_ref (e.g., "FIXED_PROSTHESIS") */
    private String workFamily;

    /** Human-readable label for the work family (from lookup) */
    private String familyLabel;

    /** Human-readable label for the work type (from lookup) */
    private String typeLabel;

    /** Optional text description (e.g., "Stratified disilicate crown") */
    private String description;

    private String shade;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long clientId;
    private Long orderId;

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getWorkFamily() { return workFamily; }
    public void setWorkFamily(String workFamily) { this.workFamily = workFamily; }

    public String getFamilyLabel() { return familyLabel; }
    public void setFamilyLabel(String familyLabel) { this.familyLabel = familyLabel; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShade() { return shade; }
    public void setShade(String shade) { this.shade = shade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
