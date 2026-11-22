package com.nlogistic.service;

import com.nlogistic.dao.*;
import com.nlogistic.dao.impl.*;
import com.nlogistic.db_config.GetConnection;
import com.nlogistic.model.*;
import com.nlogistic.util.DocumentStorageUtil;
import com.nlogistic.util.FileValidationUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class DocumentService {

    private final DocumentDAO documentDAO;
    private final DocumentTypeDAO documentTypeDAO;
    private final OfferDAO offerDAO;
    private final CompanyDAO companyDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final StudentDAO studentDAO;
    private final AuditService auditService;
    private final AuthorizationService authzService;

    public DocumentService() {
        this.documentDAO = new DocumentDAOImpl();
        this.documentTypeDAO = new DocumentTypeDAOImpl();
        this.offerDAO = new OfferDAOImpl();
        this.companyDAO = new CompanyDAOImpl();
        this.placementDriveDAO = new PlacementDriveDAOImpl();
        this.studentDAO = new StudentDAOImpl();
        this.auditService = new AuditService();
        this.authzService = new AuthorizationService();
    }

    public DocumentService(DocumentDAO documentDAO, DocumentTypeDAO documentTypeDAO, OfferDAO offerDAO,
                           CompanyDAO companyDAO, PlacementDriveDAO placementDriveDAO, StudentDAO studentDAO,
                           AuditService auditService, AuthorizationService authzService) {
        this.documentDAO = documentDAO;
        this.documentTypeDAO = documentTypeDAO;
        this.offerDAO = offerDAO;
        this.companyDAO = companyDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.studentDAO = studentDAO;
        this.auditService = auditService;
        this.authzService = authzService;
    }

    /**
     * Data Transfer Object for Document Upload submission
     */
    public static class UploadRequest {
        public Long companyId;
        public Long driveId;
        public Integer documentTypeId;
        public Long studentId;
        public String offerType;
        public BigDecimal ctc;
        public BigDecimal stipend;
        public LocalDate offerDate;
        public LocalDate joiningDate;
        public String status;
        public Part filePart;
        public Long uploadedByUserId;
        public ServletContext servletContext;
        public HttpServletRequest httpRequest;
    }

    public static class DownloadResource {
        private final Path filePath;
        private final String originalFileName;
        private final String mimeType;
        private final long fileSize;

        public DownloadResource(Path filePath, String originalFileName, String mimeType, long fileSize) {
            this.filePath = filePath;
            this.originalFileName = originalFileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }

        public Path getFilePath() { return filePath; }
        public String getOriginalFileName() { return originalFileName; }
        public String getMimeType() { return mimeType; }
        public long getFileSize() { return fileSize; }
    }

    /**
     * Core upload function handling both general recruitment documents and student offer letters.
     * Ensures strict atomicity: if DB insertion fails, newly created physical file is cleaned up.
     */
    public Long uploadDocument(UploadRequest req) throws Exception {
        if (req == null) {
            throw new IllegalArgumentException("Upload request cannot be null.");
        }
        if (req.companyId == null || req.companyId <= 0) {
            throw new IllegalArgumentException("Validation Error: Company selection is required.");
        }
        if (req.driveId == null || req.driveId <= 0) {
            throw new IllegalArgumentException("Validation Error: Placement Drive selection is required.");
        }
        if (req.documentTypeId == null || req.documentTypeId <= 0) {
            throw new IllegalArgumentException("Validation Error: Document category is required.");
        }

        // 1. Verify Company exists
        Optional<Company> companyOpt = companyDAO.findById(req.companyId);
        if (companyOpt.isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Selected Company (ID " + req.companyId + ") does not exist.");
        }

        // 2. Verify Placement Drive exists and belongs to the selected company
        Optional<PlacementDrive> driveOpt = placementDriveDAO.findById(req.driveId);
        if (driveOpt.isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Selected Placement Drive (ID " + req.driveId + ") does not exist.");
        }
        PlacementDrive drive = driveOpt.get();
        if (!drive.getCompanyId().equals(req.companyId)) {
            throw new IllegalArgumentException("Validation Error: Drive (ID " + req.driveId + ") does not belong to Company (ID " + req.companyId + ").");
        }

        // 3. Category-specific validation: Offer Letter (ID = 4)
        boolean isOfferLetter = (req.documentTypeId == 4);
        StudentProfile studentProfile = null;
        if (isOfferLetter) {
            if (req.studentId == null || req.studentId <= 0) {
                throw new IllegalArgumentException("Validation Error: A student must be selected for Offer Letter uploads.");
            }
            Optional<StudentProfile> spOpt = studentDAO.findById(req.studentId);
            if (spOpt.isEmpty()) {
                throw new IllegalArgumentException("Validation Error: Student (ID " + req.studentId + ") not found.");
            }
            studentProfile = spOpt.get();
        }

        // 4. File validation (size, extension, mime, magic-bytes)
        FileValidationUtil.validateFile(req.filePart, req.documentTypeId);

        // 5. File storage preparation
        Path storageRoot = DocumentStorageUtil.getStorageRoot(req.servletContext);
        Path targetDir = DocumentStorageUtil.resolveDirectory(storageRoot, req.companyId, req.driveId, req.documentTypeId);
        String rawOriginalName = req.filePart.getSubmittedFileName();
        String originalFileName = FileValidationUtil.sanitizeFileName(rawOriginalName);
        String storedFileName = DocumentStorageUtil.generateStoredFileName(req.documentTypeId, req.studentId, originalFileName);
        Path targetFilePath = targetDir.resolve(storedFileName).normalize();

        // 6. Write physical file using Java File I/O
        DocumentStorageUtil.saveFile(req.filePart, targetFilePath, storageRoot);

        // 7. Database transaction
        Path newlyCreatedFile = targetFilePath;
        Long generatedDocumentId = null;

        try (Connection conn = GetConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long offerId = null;
                Long applicationId = null;

                if (isOfferLetter) {
                    // Map offer to student: ensures application and offer record exist
                    applicationId = offerDAO.findOrCreateApplication(req.driveId, req.studentId, conn);

                    // Check if an offer already exists for student and drive
                    Optional<Offer> existingOfferOpt = offerDAO.findByStudentAndDrive(req.studentId, req.driveId, conn);
                    if (existingOfferOpt.isPresent()) {
                        Offer existing = existingOfferOpt.get();
                        existing.setOfferType(req.offerType != null ? req.offerType : "FULL_TIME");
                        if (req.ctc != null) existing.setCtc(req.ctc);
                        if (req.stipend != null) existing.setStipend(req.stipend);
                        if (req.offerDate != null) existing.setOfferDate(req.offerDate);
                        if (req.joiningDate != null) existing.setJoiningDate(req.joiningDate);
                        existing.setStatus(req.status != null ? req.status : "OFFERED");
                        offerDAO.updateOffer(existing, conn);
                        offerId = existing.getOfferId();
                    } else {
                        Offer newOffer = new Offer();
                        newOffer.setApplicationId(applicationId);
                        newOffer.setStudentId(req.studentId);
                        newOffer.setCompanyId(req.companyId);
                        newOffer.setDriveId(req.driveId);
                        newOffer.setOfferType(req.offerType != null ? req.offerType : "FULL_TIME");
                        newOffer.setCtc(req.ctc);
                        newOffer.setStipend(req.stipend);
                        newOffer.setOfferDate(req.offerDate != null ? req.offerDate : LocalDate.now());
                        newOffer.setJoiningDate(req.joiningDate);
                        newOffer.setStatus(req.status != null ? req.status : "OFFERED");
                        newOffer.setIsAccepted(false);
                        offerId = offerDAO.createOffer(newOffer, conn);
                    }

                    // Update application status to OFFERED
                    String updateAppSql = "UPDATE applications SET application_status = 'OFFERED' WHERE application_id = ?";
                    try (PreparedStatement psApp = conn.prepareStatement(updateAppSql)) {
                        psApp.setLong(1, applicationId);
                        psApp.executeUpdate();
                    }
                }

                // Insert document metadata
                Document doc = new Document();
                doc.setDocumentTypeId(req.documentTypeId);
                doc.setCompanyId(req.companyId);
                doc.setDriveId(req.driveId);
                doc.setStudentId(req.studentId);
                doc.setApplicationId(applicationId);
                doc.setOfferId(offerId);
                doc.setUploadedBy(req.uploadedByUserId != null ? req.uploadedByUserId : 1L);
                doc.setOriginalFileName(originalFileName);
                doc.setStoredFileName(storedFileName);
                doc.setFilePath(targetFilePath.toString());
                doc.setMimeType(req.filePart.getContentType() != null ? req.filePart.getContentType() : "application/pdf");
                doc.setFileSize(req.filePart.getSize());

                generatedDocumentId = documentDAO.createDocument(doc, conn);

                // Audit logging
                String actionName = isOfferLetter ? "OFFER_LETTER_UPLOADED" : "PLACEMENT_DOCUMENT_UPLOADED";
                String details = String.format("Uploaded %s (Type ID: %d) for Company %s, Drive %s",
                        originalFileName, req.documentTypeId, companyOpt.get().getCompanyName(), drive.getJobTitle());
                auditService.logAction(req.uploadedByUserId, actionName, "documents", generatedDocumentId, null, details, req.httpRequest);

                conn.commit();
                return generatedDocumentId;

            } catch (Exception ex) {
                conn.rollback();
                // Clean up physical file on rollback to avoid orphan files
                DocumentStorageUtil.deleteFileQuietly(newlyCreatedFile);
                throw ex;
            }
        }
    }

    /**
     * Map offer to student helper method
     */
    public Long mapOfferToStudent(Long studentId, Long driveId, Long companyId, Offer offerDetails, Connection conn) {
        if (studentId == null || driveId == null || companyId == null) {
            throw new IllegalArgumentException("Student ID, Drive ID, and Company ID are required for offer mapping.");
        }

        Long appId = offerDAO.findOrCreateApplication(driveId, studentId, conn);

        Optional<Offer> existingOpt = offerDAO.findByStudentAndDrive(studentId, driveId, conn);
        if (existingOpt.isPresent()) {
            Offer existing = existingOpt.get();
            if (offerDetails != null) {
                if (offerDetails.getOfferType() != null) existing.setOfferType(offerDetails.getOfferType());
                if (offerDetails.getCtc() != null) existing.setCtc(offerDetails.getCtc());
                if (offerDetails.getStipend() != null) existing.setStipend(offerDetails.getStipend());
                if (offerDetails.getOfferDate() != null) existing.setOfferDate(offerDetails.getOfferDate());
                if (offerDetails.getJoiningDate() != null) existing.setJoiningDate(offerDetails.getJoiningDate());
                if (offerDetails.getStatus() != null) existing.setStatus(offerDetails.getStatus());
                offerDAO.updateOffer(existing, conn);
            }
            return existing.getOfferId();
        }

        Offer newOffer = new Offer();
        newOffer.setApplicationId(appId);
        newOffer.setStudentId(studentId);
        newOffer.setCompanyId(companyId);
        newOffer.setDriveId(driveId);
        if (offerDetails != null) {
            newOffer.setOfferType(offerDetails.getOfferType() != null ? offerDetails.getOfferType() : "FULL_TIME");
            newOffer.setCtc(offerDetails.getCtc());
            newOffer.setStipend(offerDetails.getStipend());
            newOffer.setOfferDate(offerDetails.getOfferDate() != null ? offerDetails.getOfferDate() : LocalDate.now());
            newOffer.setJoiningDate(offerDetails.getJoiningDate());
            newOffer.setStatus(offerDetails.getStatus() != null ? offerDetails.getStatus() : "OFFERED");
        } else {
            newOffer.setOfferType("FULL_TIME");
            newOffer.setOfferDate(LocalDate.now());
            newOffer.setStatus("OFFERED");
        }
        Long createdOfferId = offerDAO.createOffer(newOffer, conn);
        String updateAppSql = "UPDATE applications SET application_status = 'OFFERED' WHERE application_id = ?";
        try (PreparedStatement psApp = conn.prepareStatement(updateAppSql)) {
            psApp.setLong(1, appId);
            psApp.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating application status: " + e.getMessage(), e);
        }
        return createdOfferId;
    }

    /**
     * Retrieve documents for a particular company, drive, category, and search query.
     */
    public List<Document> getCompanyDocuments(Long companyId, Long driveId, Integer documentTypeId, String search) {
        return documentDAO.findFiltered(companyId, driveId, documentTypeId, search);
    }

    /**
     * Retrieve document by ID
     */
    public Optional<Document> getDocumentById(Long documentId) {
        if (documentId == null || documentId <= 0) return Optional.empty();
        return documentDAO.findById(documentId);
    }

    /**
     * Retrieve all offer letters belonging to a specific student
     */
    public List<Document> getStudentOfferLetters(Long studentId) {
        if (studentId == null || studentId <= 0) return Collections.emptyList();
        return documentDAO.findOfferLettersByStudentId(studentId);
    }

    /**
     * Retrieve offer details list for student
     */
    public List<Offer> getOffersByStudentId(Long studentId) {
        if (studentId == null || studentId <= 0) return Collections.emptyList();
        return offerDAO.findByStudentId(studentId);
    }

    /**
     * Dashboard metrics for Placement Executive
     */
    public Map<String, Object> getExecutiveDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", documentDAO.countTotalDocuments());
        stats.put("totalOfferLetters", documentDAO.countTotalOfferLetters());
        stats.put("totalCompaniesWithDocs", documentDAO.countCompaniesWithDocuments());
        stats.put("totalDrivesWithDocs", documentDAO.countDrivesWithDocuments());
        stats.put("recentDocuments", documentDAO.findRecentDocuments(10));
        return stats;
    }

    /**
     * Secure Document Download Process:
     * 1. Validates document existence in database.
     * 2. Enforces Role-Based Access Control:
     *    - Student can strictly download ONLY their own offer letter / document.
     *    - Executive / Coordinator / HOD / Committee / Consultant can access company/drive placement documents.
     * 3. Validates physical file existence on disk.
     * 4. Enforces path traversal check against allowed storage root.
     * 5. Logs audit action.
     */
    public DownloadResource downloadDocument(Long documentId, User user, ServletContext servletContext, HttpServletRequest req) {
        if (documentId == null || documentId <= 0) {
            throw new IllegalArgumentException("Document not found: Invalid document ID.");
        }
        if (user == null) {
            throw new SecurityException("Unauthorized: User session is required to download documents.");
        }

        Optional<Document> docOpt = documentDAO.findById(documentId);
        if (docOpt.isEmpty()) {
            throw new NoSuchElementException("Document not found: Document ID #" + documentId + " does not exist.");
        }
        Document doc = docOpt.get();

        // Authorization check
        if (authzService.isStudent(user)) {
            // Find student profile for user
            Optional<StudentProfile> spOpt = studentDAO.findByUserId(user.getUserId());
            if (spOpt.isEmpty()) {
                auditService.logAccessDenied(user.getUserId(), "DOCUMENT_DOWNLOAD_" + documentId, req);
                throw new SecurityException("Access denied: No student profile associated with current account.");
            }
            Long loggedInStudentId = spOpt.get().getStudentId();
            if (doc.getStudentId() == null || !doc.getStudentId().equals(loggedInStudentId)) {
                auditService.logAccessDenied(user.getUserId(), "DOCUMENT_DOWNLOAD_OTHER_STUDENT_" + documentId, req);
                throw new SecurityException("Access denied: You are not authorized to view or download another student's document.");
            }
        } else if (authzService.isPlacementExecutive(user) || authzService.isPlacementCoordinator(user)
                || authzService.isHod(user) || authzService.isCommitteeMember(user) || authzService.isConsultant(user)) {
            // Authorized staff
        } else {
            auditService.logAccessDenied(user.getUserId(), "DOCUMENT_DOWNLOAD_" + documentId, req);
            throw new SecurityException("Access denied: User does not possess permission to download placement documents.");
        }

        // Physical file verification
        if (doc.getFilePath() == null || doc.getFilePath().trim().isEmpty()) {
            throw new NoSuchElementException("Document file record is incomplete: File path is missing in database.");
        }

        Path filePath = Paths.get(doc.getFilePath()).toAbsolutePath().normalize();

        // If file does not exist at stored path, check fallback storage locations (handles server/workspace migration)
        if (!Files.exists(filePath)) {
            Path fallback = DocumentStorageUtil.findFallbackFile(doc.getStoredFileName(), servletContext);
            if (fallback != null && Files.exists(fallback)) {
                filePath = fallback;
            }
        }

        // Comprehensive Path Traversal & Allowed Storage Security Verification
        DocumentStorageUtil.validateDownloadPath(filePath, servletContext);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new NoSuchElementException("File not found on server storage: The physical file could not be located.");
        }

        // Audit log download
        auditService.logAction(user.getUserId(), "DOCUMENT_DOWNLOADED", "documents", documentId, null,
                "Downloaded " + doc.getOriginalFileName() + " (Doc ID: " + documentId + ")", req);

        String mime = (doc.getMimeType() != null && !doc.getMimeType().trim().isEmpty())
                ? doc.getMimeType() : "application/octet-stream";

        return new DownloadResource(filePath, doc.getOriginalFileName(), mime, doc.getFileSize());
    }

    // Master data lookups
    public List<Company> getAllCompanies() {
        return companyDAO.findAll();
    }

    public List<PlacementDrive> getDrivesByCompanyId(Long companyId) {
        if (companyId == null || companyId <= 0) return Collections.emptyList();
        List<PlacementDrive> all = placementDriveDAO.findAll();
        List<PlacementDrive> filtered = new ArrayList<>();
        for (PlacementDrive d : all) {
            if (companyId.equals(d.getCompanyId())) {
                filtered.add(d);
            }
        }
        return filtered;
    }

    public List<DocumentType> getAllDocumentTypes() {
        return documentTypeDAO.findAll();
    }

    public List<StudentProfile> getStudentsForDrive(Long driveId) {
        if (driveId == null || driveId <= 0) {
            return studentDAO.findAll();
        }
        List<StudentProfile> driveStudents = studentDAO.findStudentsByDriveId(driveId);
        if (driveStudents.isEmpty()) {
            return studentDAO.findAll();
        }
        return driveStudents;
    }

    public Optional<StudentProfile> getStudentProfileByUserId(Long userId) {
        return studentDAO.findByUserId(userId);
    }
}
