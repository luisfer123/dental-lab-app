package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "building_status_history")
public class BuildingStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private WorkEntity work;

    @Column(name = "extension_type", nullable = false, length = 30)
    private String extensionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_status_id")
    private BuildingStatusRefEntity fromStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_status_id", nullable = false)
    private BuildingStatusRefEntity toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_worker_id")
    private WorkerEntity changedByWorker;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "note")
    private String note;

    // ==========================
    // Getters / Setters
    // ==========================

    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long historyId) { this.historyId = historyId; }

    public WorkEntity getWork() { return work; }
    public void setWork(WorkEntity work) { this.work = work; }

    public String getExtensionType() { return extensionType; }
    public void setExtensionType(String extensionType) { this.extensionType = extensionType; }

    public BuildingStatusRefEntity getFromStatus() { return fromStatus; }
    public void setFromStatus(BuildingStatusRefEntity fromStatus) { this.fromStatus = fromStatus; }

    public BuildingStatusRefEntity getToStatus() { return toStatus; }
    public void setToStatus(BuildingStatusRefEntity toStatus) { this.toStatus = toStatus; }

    public WorkerEntity getChangedByWorker() { return changedByWorker; }
    public void setChangedByWorker(WorkerEntity changedByWorker) { this.changedByWorker = changedByWorker; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildingStatusHistoryEntity)) return false;
        BuildingStatusHistoryEntity that = (BuildingStatusHistoryEntity) o;
        return Objects.equals(historyId, that.historyId);
    }

    @Override
    public int hashCode() { return Objects.hash(historyId); }

    @Override
    public String toString() {
        return "BuildingStatusHistoryEntity{" +
                "historyId=" + historyId +
                ", extensionType='" + extensionType + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
