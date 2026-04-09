package com.digitalfir.backend.model;
import com.digitalfir.backend.model.FirStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fir")
public class FIR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String complainantName;

    @Column(columnDefinition = "TEXT")
    private String complaintDetails;

    @Enumerated(EnumType.STRING)
    private FirStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long createdBy;

    @PrePersist
    public void onCreate() {
        if (this.status == null) {
            this.status = FirStatus.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getComplainantName() {
        return complainantName;
    }

    public void setComplainantName(String complainantName) {
        this.complainantName = complainantName;
    }

    public String getComplaintDetails() {
        return complaintDetails;
    }

    public void setComplaintDetails(String complaintDetails) {
        this.complaintDetails = complaintDetails;
    }

    public FirStatus getStatus() {
        return status;
    }

    public void setStatus(FirStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}



