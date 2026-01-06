package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single dental work item (e.g., crown, bridge, inlay)
 * using lookup tables for work_family, type, status.
 */
@Entity
@Table(
    name = "work",
    indexes = {
        @Index(name = "idx_work_order", columnList = "order_id"),
        @Index(name = "idx_work_client", columnList = "client_id"),
        @Index(name = "idx_work_family", columnList = "work_family"),
        @Index(name = "idx_work_type", columnList = "type")
    }
)
public class WorkEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==========================================================
    // CORE FIELDS
    // ==========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private WorkOrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "work_id",
        referencedColumnName = "work_id",
        insertable = false,
        updatable = false
    )
    private WorkPriceEntity price;

    // ----------------------------------------------------------
    // 🔥 Lookup references instead of strings
    // ----------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_family", referencedColumnName = "code", nullable = false)
    private WorkFamilyRefEntity workFamily;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type", referencedColumnName = "code", nullable = false)
    private WorkTypeRefEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status", referencedColumnName = "code")
    private WorkStatusRefEntity status;

    // ----------------------------------------------------------

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "shade", length = 50)
    private String shade;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ==========================================================
    // HUMAN-READABLE INTERNAL CODE (ID)
    // ==========================================================
    
    @Column(name = "profile_prefix", length = 1)
    private String profilePrefix;

    @Column(name = "client_profile_id")
    private Long clientProfileId;

    @Column(name = "internal_seq")
    private Integer internalSeq;

    @Column(name = "internal_year")
    private Integer internalYear;

    @Column(name = "internal_code", length = 50)
    private String internalCode;

    // ==========================================================
    // RELATIONSHIPS
    // ==========================================================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "work_work_category",
        joinColumns = @JoinColumn(name = "work_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<WorkCategoryEntity> categories = new ArrayList<>();

    @OneToOne(mappedBy = "work", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CrownWorkEntity crownWork;

    @OneToOne(mappedBy = "work", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private BridgeWorkEntity bridgeWork;

    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    public WorkEntity() {}

    public WorkEntity(WorkOrderEntity order, ClientEntity client,
                      WorkTypeRefEntity type, WorkFamilyRefEntity family) {
        this.order = order;
        this.client = client;
        this.type = type;
        this.workFamily = family;
    }

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkOrderEntity getOrder() { return order; }
    public void setOrder(WorkOrderEntity order) { this.order = order; }

    public ClientEntity getClient() { return client; }
    public void setClient(ClientEntity client) { this.client = client; }
    
    public WorkPriceEntity getPrice() { return price; }
    public void setPrice(WorkPriceEntity price) { this.price = price; }

    public WorkFamilyRefEntity getWorkFamily() { return workFamily; }
    public void setWorkFamily(WorkFamilyRefEntity workFamily) { this.workFamily = workFamily; }

    public WorkTypeRefEntity getType() { return type; }
    public void setType(WorkTypeRefEntity type) { this.type = type; }

    public WorkStatusRefEntity getStatus() { return status; }
    public void setStatus(WorkStatusRefEntity status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShade() { return shade; }
    public void setShade(String shade) { this.shade = shade; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<WorkCategoryEntity> getCategories() { return categories; }
    public void setCategories(List<WorkCategoryEntity> categories) { this.categories = categories; }
    
    public String getProfilePrefix() { return profilePrefix; }
    public void setProfilePrefix(String profilePrefix) { this.profilePrefix = profilePrefix; }

    public Long getClientProfileId() { return clientProfileId; }
    public void setClientProfileId(Long id) { this.clientProfileId = id; }

    public Integer getInternalSeq() { return internalSeq; }
    public void setInternalSeq(Integer seq) { this.internalSeq = seq; }

    public Integer getInternalYear() { return internalYear; }
    public void setInternalYear(Integer year) { this.internalYear = year; }

    public String getInternalCode() { return internalCode; }
    public void setInternalCode(String code) { this.internalCode = code; }

    public CrownWorkEntity getCrownWork() { return crownWork; }
    public void setCrownWork(CrownWorkEntity crownWork) { this.crownWork = crownWork; }

    public BridgeWorkEntity getBridgeWork() { return bridgeWork; }
    public void setBridgeWork(BridgeWorkEntity bridgeWork) { this.bridgeWork = bridgeWork; }

    // ==========================================================
    // HELPER METHODS
    // ==========================================================

    public void addCategory(WorkCategoryEntity category) {
        if (!categories.contains(category)) {
            categories.add(category);
            category.getWorks().add(this);
        }
    }

    public void removeCategory(WorkCategoryEntity category) {
        if (categories.remove(category)) {
            category.getWorks().remove(this);
        }
    }

    // ==========================================================
    // EQUALITY
    // ==========================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkEntity)) return false;
        WorkEntity that = (WorkEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // ==========================================================
    // TO STRING
    // ==========================================================

    @Override
    public String toString() {
        return "WorkEntity{" +
        		"id:" + id +
                ", internalCode=" + (internalCode != null ? internalCode : null) +
                ", family=" + (workFamily != null ? workFamily.getCode() : null) +
                ", type=" + (type != null ? type.getCode() : null) +
                ", description='" + description + '\'' +
                ", shade='" + shade + '\'' +
                ", status=" + (status != null ? status.getCode() : null) +
                '}';
    }
}
