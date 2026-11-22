package com.nlogistic.dao;

import com.nlogistic.model.Document;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface DocumentDAO {
    Long createDocument(Document doc);
    Long createDocument(Document doc, Connection conn);
    Optional<Document> findById(Long documentId);
    Optional<Document> findById(Long documentId, Connection conn);
    List<Document> findByCompanyId(Long companyId);
    Optional<Document> findByDriveId(Long driveId);
    Optional<Document> findByDriveId(Long driveId, Connection conn);
    Optional<Document> findLatestResumeByStudentId(Long studentId);
    Optional<Document> findLatestResumeByStudentId(Long studentId, Connection conn);
    List<Document> findByStudentId(Long studentId);

    // Module 8 Enhancements
    List<Document> findFiltered(Long companyId, Long driveId, Integer documentTypeId, String searchKeyword);
    List<Document> findOfferLettersByStudentId(Long studentId);
    List<Document> findRecentDocuments(int limit);
    int countTotalDocuments();
    int countTotalOfferLetters();
    int countCompaniesWithDocuments();
    int countDrivesWithDocuments();
    boolean deleteDocument(Long documentId);
    boolean deleteDocument(Long documentId, Connection conn);
    boolean updateOfferId(Long documentId, Long offerId, Connection conn);
    List<Document> findVaultDocumentsByStudentId(Long studentId);
    boolean archiveDocument(Long documentId, Long supersededByDocId, Connection conn);
}
