package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a category or label that can be applied to a dental work.
 * Example: DIGITAL, ESTHETIC, METAL_FREE, FIXED_PROSTHESIS, etc.
 */
@Entity
@Table(name = "work_category")
public class WorkCategoryEntity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<WorkEntity> works = new ArrayList<>();

    // ===== Constructors =====
    public WorkCategoryEntity() {}
    public WorkCategoryEntity(String name) { this.name = name; }

    // ===== Getters and Setters (grouped per field) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<WorkEntity> getWorks() { return works; }
    public void setWorks(List<WorkEntity> works) { this.works = works; }

    // ===== Helper methods =====
    public void addWork(WorkEntity work) {
        if (!works.contains(work)) {
            works.add(work);
            work.getCategories().add(this);
        }
    }

    public void removeWork(WorkEntity work) {
        if (works.contains(work)) {
            works.remove(work);
            work.getCategories().remove(this);
        }
    }

    // ===== equals / hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkCategoryEntity)) return false;
        WorkCategoryEntity that = (WorkCategoryEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // ===== toString =====
    @Override
    public String toString() {
        return "WorkCategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
