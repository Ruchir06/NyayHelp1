package com.nyayhelp.userservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authUserId;
    private String email;
    private String role;
    private String name;
    private String location;
    private String category;
    private Integer experience;
    private Double fees;
    private String verificationStatus;
    private String documents;
    private String barCouncilId;

    @Column(columnDefinition = "LONGTEXT")
    private String livePhotoBase64;

    @Column(columnDefinition = "LONGTEXT")
    private String aadhaarBase64;

    @Column(columnDefinition = "LONGTEXT")
    private String barCouncilBase64;

    @Column(columnDefinition = "LONGTEXT")
    private String licenseBase64;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    public String getLivePhotoBase64() { return livePhotoBase64; }
    public void setLivePhotoBase64(String livePhotoBase64) { this.livePhotoBase64 = livePhotoBase64; }

    public String getAadhaarBase64() { return aadhaarBase64; }
    public void setAadhaarBase64(String aadhaarBase64) { this.aadhaarBase64 = aadhaarBase64; }

    public String getBarCouncilBase64() { return barCouncilBase64; }
    public void setBarCouncilBase64(String barCouncilBase64) { this.barCouncilBase64 = barCouncilBase64; }

    public String getLicenseBase64() { return licenseBase64; }
    public void setLicenseBase64(String licenseBase64) { this.licenseBase64 = licenseBase64; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(Long authUserId) {
        this.authUserId = authUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Double getFees() {
        return fees;
    }

    public void setFees(Double fees) {
        this.fees = fees;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getDocuments() {
        return documents;
    }

    public void setDocuments(String documents) {
        this.documents = documents;
    }

    public String getBarCouncilId() {
    return barCouncilId;
}

public void setBarCouncilId(String barCouncilId) {
    this.barCouncilId = barCouncilId;
}


}