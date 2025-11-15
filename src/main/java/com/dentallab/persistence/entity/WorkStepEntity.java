package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a specific step or stage in the production process
 * of a dental work (e.g., design, casting, ceramic layering, polishing).
 */
@Entity
@Table(name = "work_step")
public class WorkStepEntity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private WorkEntity work;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private WorkerEntity worker;

    @Column(name = "step_type", length = 50)
    private String stepType;

    @Column(name = "date_started")
    private LocalDateTime dateStarted;

    @Column(name = "date_completed")
    private LocalDateTime dateCompleted;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===== Constructors =====
    public WorkStepEntity() {}
    public WorkStepEntity(WorkEntity work, String stepType) {
        this.work = work;
        this.stepType = stepType;
    }

    // ===== Getters and Setters (grouped per field) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkEntity getWork() { return work; }
    public void setWork(WorkEntity work) { this.work = work; }

    public WorkerEntity getWorker() { return worker; }
    public void setWorker(WorkerEntity worker) { this.worker = worker; }

    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }

    public LocalDateTime getDateStarted() { return dateStarted; }
    public void setDateStarted(LocalDateTime dateStarted) { this.dateStarted = dateStarted; }

    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(LocalDateTime dateCompleted) { this.dateCompleted = dateCompleted; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ===== equals / hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkStepEntity)) return false;
        WorkStepEntity that = (WorkStepEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // ===== toString =====
    @Override
    public String toString() {
        return "WorkStepEntity{" +
                "id=" + id +
                ", work=" + (work != null ? work.getId() : null) +
                ", worker=" + (worker != null ? worker.getId() : null) +
                ", stepType='" + stepType + '\'' +
                ", dateStarted=" + dateStarted +
                ", dateCompleted=" + dateCompleted +
                '}';
    }
}
