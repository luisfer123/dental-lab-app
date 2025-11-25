package com.dentallab.api.model;

import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

public class WorkOrderModel extends RepresentationModel<WorkOrderModel> {

    private Long id;

    private Long clientId;
    private String clientName;

    private LocalDateTime dateReceived;
    private LocalDateTime dueDate;
    private LocalDateTime deliveredAt;

    private String status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: a list of works belonging to this order
    private List<Long> workIds;

    public WorkOrderModel() {
    }

    // Getters and setters below

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

    public List<Long> getWorkIds() {
        return workIds;
    }

    public void setWorkIds(List<Long> workIds) {
        this.workIds = workIds;
    }
    
    @Override
    public String toString() {
		return "WorkOrderModel{" +
				"id=" + id +
				", clientId=" + clientId +
				", clientName='" + clientName + '\'' +
				", dateReceived=" + dateReceived +
				", dueDate=" + dueDate +
				", deliveredAt=" + deliveredAt +
				", status='" + status + '\'' +
				", notes='" + notes + '\'' +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", workIds=" + workIds +
				'}';
	}
    
    @Override
    public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WorkOrderModel)) return false;

		WorkOrderModel that = (WorkOrderModel) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
		if (clientName != null ? !clientName.equals(that.clientName) : that.clientName != null) return false;
		if (dateReceived != null ? !dateReceived.equals(that.dateReceived) : that.dateReceived != null) return false;
		if (dueDate != null ? !dueDate.equals(that.dueDate) : that.dueDate != null) return false;
		if (deliveredAt != null ? !deliveredAt.equals(that.deliveredAt) : that.deliveredAt != null) return false;
		if (status != null ? !status.equals(that.status) : that.status != null) return false;
		if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
		if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
		if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;
		return workIds != null ? workIds.equals(that.workIds) : that.workIds == null;
	}
    
    @Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
		result = 31 * result + (clientName != null ? clientName.hashCode() : 0);
		result = 31 * result + (dateReceived != null ? dateReceived.hashCode() : 0);
		result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
		result = 31 * result + (deliveredAt != null ? deliveredAt.hashCode() : 0);
		result = 31 * result + (status != null ? status.hashCode() : 0);
		result = 31 * result + (notes != null ? notes.hashCode() : 0);
		result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
		result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
		result = 31 * result + (workIds != null ? workIds.hashCode() : 0);
		return result;
	}
    
}
