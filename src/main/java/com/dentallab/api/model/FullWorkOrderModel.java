package com.dentallab.api.model;

import org.springframework.hateoas.RepresentationModel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full representation of a Work Order including client info
 * and embedded works.
 */
public class FullWorkOrderModel extends RepresentationModel<FullWorkOrderModel> {

    private Long id;

    // ----------- Client Summary -----------
    private Long clientId;
    private String clientName;
    private String clientPrimaryEmail;
    private String clientPrimaryPhone;

    // ----------- Timestamps -----------
    private LocalDateTime dateReceived;
    private LocalDateTime dueDate;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String status;
    private String notes;

    // ----------- Embedded Works -----------
    private List<WorkModel> works;

    public FullWorkOrderModel() {}

    // ---------- Getters / Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPrimaryEmail() {
        return clientPrimaryEmail;
    }

    public void setClientPrimaryEmail(String clientPrimaryEmail) {
        this.clientPrimaryEmail = clientPrimaryEmail;
    }

    public String getClientPrimaryPhone() {
        return clientPrimaryPhone;
    }

    public void setClientPrimaryPhone(String clientPrimaryPhone) {
        this.clientPrimaryPhone = clientPrimaryPhone;
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

    public List<WorkModel> getWorks() {
        return works;
    }

    public void setWorks(List<WorkModel> works) {
        this.works = works;
    }
}
