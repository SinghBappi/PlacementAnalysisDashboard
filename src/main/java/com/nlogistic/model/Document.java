package com.nlogistic.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long documentId;
    private Integer documentTypeId;
    private Long studentId;
    private Long companyId;
    private Long driveId;
    private Long applicationId;
    private Long offerId;
    private Long uploadedBy;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    // Joined presentation fields
    private String documentTypeName;
    private String companyName;
    private String driveTitle;
    private String studentName;
    private String studentRollNumber;
    private String studentEmail;
    private String uploadedByName;
    private String offerType;
    private BigDecimal offerCtc;
    private BigDecimal offerStipend;
    private String offerStatus;

    // Versioning and active marksheet tracking
    private Boolean isArchived = false;
    private LocalDateTime archivedAt;
    private Integer semester;
    private Integer version = 1;
    private Long supersededByDocumentId;
    private String verificationStatus;

    public Document() {}

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Integer getDocumentTypeId() { return documentTypeId; }
    public void setDocumentTypeId(Integer documentTypeId) { this.documentTypeId = documentTypeId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getDriveId() { return driveId; }
    public void setDriveId(Long driveId) { this.driveId = driveId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getOfferId() { return offerId; }
    public void setOfferId(Long offerId) { this.offerId = offerId; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getDocumentTypeName() { return documentTypeName; }
    public void setDocumentTypeName(String documentTypeName) { this.documentTypeName = documentTypeName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDriveTitle() { return driveTitle; }
    public void setDriveTitle(String driveTitle) { this.driveTitle = driveTitle; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }

    public String getOfferType() { return offerType; }
    public void setOfferType(String offerType) { this.offerType = offerType; }

    public BigDecimal getOfferCtc() { return offerCtc; }
    public void setOfferCtc(BigDecimal offerCtc) { this.offerCtc = offerCtc; }

    public BigDecimal getOfferStipend() { return offerStipend; }
    public void setOfferStipend(BigDecimal offerStipend) { this.offerStipend = offerStipend; }

    public String getOfferStatus() { return offerStatus; }
    public void setOfferStatus(String offerStatus) { this.offerStatus = offerStatus; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Long getSupersededByDocumentId() { return supersededByDocumentId; }
    public void setSupersededByDocumentId(Long supersededByDocumentId) { this.supersededByDocumentId = supersededByDocumentId; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) return "0 KB";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public String getFormattedUploadedAt() {
        if (uploadedAt == null) return "N/A";
        return uploadedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getCategoryBadgeClass() {
        if (documentTypeId == null) return "bg-secondary";
        return switch (documentTypeId) {
            case 4 -> "bg-success";   // OFFER_LETTER
            case 6 -> "bg-primary";   // JOB_DESCRIPTION
            case 5 -> "bg-info text-dark"; // SELECTION_LIST
            case 9 -> "bg-warning text-dark"; // SHORTLIST
            default -> "bg-secondary";
        };
    }
}
