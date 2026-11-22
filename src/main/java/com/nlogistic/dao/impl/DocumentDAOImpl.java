package com.nlogistic.dao.impl;

import com.nlogistic.dao.DocumentDAO;
import com.nlogistic.db_config.GetConnection;
import com.nlogistic.model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentDAOImpl implements DocumentDAO {

    private static final String INSERT_DOCUMENT = """
        INSERT INTO documents (document_type_id, student_id, company_id, drive_id, application_id, offer_id, uploaded_by, original_file_name, stored_file_name, file_path, mime_type, file_size, is_archived, semester, version, superseded_by_document_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String SELECT_ENRICHED = """
        SELECT d.document_id, d.document_type_id, d.student_id, d.company_id, d.drive_id, d.application_id, d.offer_id,
               d.uploaded_by, d.original_file_name, d.stored_file_name, d.file_path, d.mime_type, d.file_size, d.uploaded_at,
               d.is_archived, d.archived_at, d.semester, d.version, d.superseded_by_document_id,
               dt.type_name AS document_type_name,
               c.company_name,
               pd.job_title AS drive_title,
               u.full_name AS student_name,
               sp.roll_number AS student_roll_number,
               u.email AS student_email,
               up.full_name AS uploaded_by_name,
               o.offer_type, o.ctc AS offer_ctc, o.stipend AS offer_stipend, o.status AS offer_status,
               adv.workflow_status AS verification_status
        FROM documents d
        JOIN document_types dt ON d.document_type_id = dt.document_type_id
        LEFT JOIN companies c ON d.company_id = c.company_id
        LEFT JOIN placement_drives pd ON d.drive_id = pd.drive_id
        LEFT JOIN student_profiles sp ON d.student_id = sp.student_id
        LEFT JOIN users u ON sp.user_id = u.user_id
        LEFT JOIN users up ON d.uploaded_by = up.user_id
        LEFT JOIN offers o ON d.offer_id = o.offer_id
        LEFT JOIN academic_document_verifications adv ON d.document_id = adv.document_id
        """;

    @Override
    public Long createDocument(Document doc) {
        try (Connection conn = GetConnection.getConnection()) {
            return createDocument(doc, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating document: " + doc.getOriginalFileName(), e);
        }
    }

    @Override
    public Long createDocument(Document doc, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, doc.getDocumentTypeId() != null ? doc.getDocumentTypeId() : 6);
            if (doc.getStudentId() != null) ps.setLong(2, doc.getStudentId()); else ps.setNull(2, Types.BIGINT);
            if (doc.getCompanyId() != null) ps.setLong(3, doc.getCompanyId()); else ps.setNull(3, Types.BIGINT);
            if (doc.getDriveId() != null) ps.setLong(4, doc.getDriveId()); else ps.setNull(4, Types.BIGINT);
            if (doc.getApplicationId() != null) ps.setLong(5, doc.getApplicationId()); else ps.setNull(5, Types.BIGINT);
            if (doc.getOfferId() != null) ps.setLong(6, doc.getOfferId()); else ps.setNull(6, Types.BIGINT);
            ps.setLong(7, doc.getUploadedBy() != null ? doc.getUploadedBy() : 1L);
            ps.setString(8, doc.getOriginalFileName());
            ps.setString(9, doc.getStoredFileName());
            ps.setString(10, doc.getFilePath());
            ps.setString(11, doc.getMimeType() != null ? doc.getMimeType() : "application/pdf");
            ps.setLong(12, doc.getFileSize() != null ? doc.getFileSize() : 0L);
            ps.setBoolean(13, Boolean.TRUE.equals(doc.getIsArchived()));
            if (doc.getSemester() != null) ps.setInt(14, doc.getSemester()); else ps.setNull(14, Types.TINYINT);
            ps.setInt(15, doc.getVersion() != null ? doc.getVersion() : 1);
            if (doc.getSupersededByDocumentId() != null) ps.setLong(16, doc.getSupersededByDocumentId()); else ps.setNull(16, Types.BIGINT);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating document with connection", e);
        }
        throw new RuntimeException("Failed to create document, no ID generated");
    }

    @Override
    public Optional<Document> findById(Long documentId) {
        try (Connection conn = GetConnection.getConnection()) {
            return findById(documentId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document by ID: " + documentId, e);
        }
    }

    @Override
    public Optional<Document> findById(Long documentId, Connection conn) {
        String sql = SELECT_ENRICHED + " WHERE d.document_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document by ID with connection", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Document> findByCompanyId(Long companyId) {
        List<Document> list = new ArrayList<>();
        String sql = SELECT_ENRICHED + " WHERE d.company_id = ? ORDER BY d.uploaded_at DESC";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding documents for company: " + companyId, e);
        }
        return list;
    }

    @Override
    public Optional<Document> findByDriveId(Long driveId) {
        try (Connection conn = GetConnection.getConnection()) {
            return findByDriveId(driveId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document by drive ID: " + driveId, e);
        }
    }

    @Override
    public Optional<Document> findByDriveId(Long driveId, Connection conn) {
        String sql = SELECT_ENRICHED + " WHERE d.drive_id = ? ORDER BY d.uploaded_at DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document by drive ID with connection", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Document> findLatestResumeByStudentId(Long studentId) {
        try (Connection conn = GetConnection.getConnection()) {
            return findLatestResumeByStudentId(studentId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest resume for student ID: " + studentId, e);
        }
    }

    @Override
    public Optional<Document> findLatestResumeByStudentId(Long studentId, Connection conn) {
        String sql = SELECT_ENRICHED + " WHERE d.student_id = ? AND d.document_type_id = 1 ORDER BY d.uploaded_at DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest resume for student ID with connection", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Document> findByStudentId(Long studentId) {
        List<Document> list = new ArrayList<>();
        String sql = SELECT_ENRICHED + " WHERE d.student_id = ? ORDER BY d.uploaded_at DESC";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding documents for student ID: " + studentId, e);
        }
        return list;
    }

    @Override
    public List<Document> findFiltered(Long companyId, Long driveId, Integer documentTypeId, String searchKeyword) {
        List<Document> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_ENRICHED).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (companyId != null && companyId > 0) {
            sql.append(" AND d.company_id = ? ");
            params.add(companyId);
        }
        if (driveId != null && driveId > 0) {
            sql.append(" AND d.drive_id = ? ");
            params.add(driveId);
        }
        if (documentTypeId != null && documentTypeId > 0) {
            sql.append(" AND d.document_type_id = ? ");
            params.add(documentTypeId);
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(d.original_file_name) LIKE ? OR LOWER(c.company_name) LIKE ? OR LOWER(pd.job_title) LIKE ? OR LOWER(u.full_name) LIKE ?) ");
            String term = "%" + searchKeyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }
        sql.append(" ORDER BY d.uploaded_at DESC");

        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findFiltered documents", e);
        }
        return list;
    }

    @Override
    public List<Document> findOfferLettersByStudentId(Long studentId) {
        List<Document> list = new ArrayList<>();
        String sql = SELECT_ENRICHED + " WHERE d.student_id = ? AND d.document_type_id = 4 ORDER BY d.uploaded_at DESC";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding offer letters for student: " + studentId, e);
        }
        return list;
    }

    @Override
    public List<Document> findRecentDocuments(int limit) {
        List<Document> list = new ArrayList<>();
        String sql = SELECT_ENRICHED + " ORDER BY d.uploaded_at DESC LIMIT ?";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding recent documents", e);
        }
        return list;
    }

    @Override
    public int countTotalDocuments() {
        String sql = "SELECT COUNT(*) FROM documents";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting total documents", e);
        }
        return 0;
    }

    @Override
    public int countTotalOfferLetters() {
        String sql = "SELECT COUNT(*) FROM documents WHERE document_type_id = 4";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting offer letters", e);
        }
        return 0;
    }

    @Override
    public int countCompaniesWithDocuments() {
        String sql = "SELECT COUNT(DISTINCT company_id) FROM documents WHERE company_id IS NOT NULL";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting companies with documents", e);
        }
        return 0;
    }

    @Override
    public int countDrivesWithDocuments() {
        String sql = "SELECT COUNT(DISTINCT drive_id) FROM documents WHERE drive_id IS NOT NULL";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting drives with documents", e);
        }
        return 0;
    }

    @Override
    public boolean deleteDocument(Long documentId) {
        try (Connection conn = GetConnection.getConnection()) {
            return deleteDocument(documentId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting document: " + documentId, e);
        }
    }

    @Override
    public boolean deleteDocument(Long documentId, Connection conn) {
        String sql = "DELETE FROM documents WHERE document_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting document: " + documentId, e);
        }
    }

    @Override
    public List<Document> findVaultDocumentsByStudentId(Long studentId) {
        List<Document> list = new ArrayList<>();
        if (studentId == null || studentId <= 0) return list;
        // Types 1: Resume, 2: Marksheet, 3: Certificate, 7: Identity, 8: Other
        String sql = SELECT_ENRICHED + " WHERE d.student_id = ? AND d.document_type_id IN (1, 2, 3, 7, 8) AND (d.is_archived = 0 OR d.is_archived IS NULL) ORDER BY d.uploaded_at DESC";
        try (Connection conn = GetConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vault documents for student: " + studentId, e);
        }
        return list;
    }

    @Override
    public boolean archiveDocument(Long documentId, Long supersededByDocId, Connection conn) {
        if (documentId == null || documentId <= 0) return false;
        String sql = "UPDATE documents SET is_archived = 1, archived_at = NOW(), superseded_by_document_id = ? WHERE document_id = ?";
        boolean closeConn = false;
        try {
            if (conn == null) {
                conn = GetConnection.getConnection();
                closeConn = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (supersededByDocId != null) ps.setLong(1, supersededByDocId); else ps.setNull(1, Types.BIGINT);
                ps.setLong(2, documentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error archiving document ID: " + documentId, e);
        } finally {
            if (closeConn && conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean updateOfferId(Long documentId, Long offerId, Connection conn) {
        String sql = "UPDATE documents SET offer_id = ? WHERE document_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (offerId != null) ps.setLong(1, offerId);
            else ps.setNull(1, Types.BIGINT);
            ps.setLong(2, documentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating offer_id on document: " + documentId, e);
        }
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setDocumentId(rs.getLong("document_id"));
        d.setDocumentTypeId(rs.getInt("document_type_id"));

        long sId = rs.getLong("student_id");
        if (!rs.wasNull()) d.setStudentId(sId);

        long cId = rs.getLong("company_id");
        if (!rs.wasNull()) d.setCompanyId(cId);

        long drId = rs.getLong("drive_id");
        if (!rs.wasNull()) d.setDriveId(drId);

        long aId = rs.getLong("application_id");
        if (!rs.wasNull()) d.setApplicationId(aId);

        long oId = rs.getLong("offer_id");
        if (!rs.wasNull()) d.setOfferId(oId);

        d.setUploadedBy(rs.getLong("uploaded_by"));
        d.setOriginalFileName(rs.getString("original_file_name"));
        d.setStoredFileName(rs.getString("stored_file_name"));
        d.setFilePath(rs.getString("file_path"));
        d.setMimeType(rs.getString("mime_type"));
        d.setFileSize(rs.getLong("file_size"));

        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) d.setUploadedAt(ts.toLocalDateTime());

        try { d.setIsArchived(rs.getBoolean("is_archived")); } catch (SQLException ignored) {}
        try {
            Timestamp aTs = rs.getTimestamp("archived_at");
            if (aTs != null) d.setArchivedAt(aTs.toLocalDateTime());
        } catch (SQLException ignored) {}
        try {
            int sem = rs.getInt("semester");
            if (!rs.wasNull()) d.setSemester(sem);
        } catch (SQLException ignored) {}
        try {
            int ver = rs.getInt("version");
            if (!rs.wasNull()) d.setVersion(ver);
        } catch (SQLException ignored) {}
        try {
            long sup = rs.getLong("superseded_by_document_id");
            if (!rs.wasNull()) d.setSupersededByDocumentId(sup);
        } catch (SQLException ignored) {}
        try { d.setVerificationStatus(rs.getString("verification_status")); } catch (SQLException ignored) {}

        // Presentation fields if available in result set
        try { d.setDocumentTypeName(rs.getString("document_type_name")); } catch (SQLException ignored) {}
        try { d.setCompanyName(rs.getString("company_name")); } catch (SQLException ignored) {}
        try { d.setDriveTitle(rs.getString("drive_title")); } catch (SQLException ignored) {}
        try { d.setStudentName(rs.getString("student_name")); } catch (SQLException ignored) {}
        try { d.setStudentRollNumber(rs.getString("student_roll_number")); } catch (SQLException ignored) {}
        try { d.setStudentEmail(rs.getString("student_email")); } catch (SQLException ignored) {}
        try { d.setUploadedByName(rs.getString("uploaded_by_name")); } catch (SQLException ignored) {}
        try { d.setOfferType(rs.getString("offer_type")); } catch (SQLException ignored) {}
        try { d.setOfferCtc(rs.getBigDecimal("offer_ctc")); } catch (SQLException ignored) {}
        try { d.setOfferStipend(rs.getBigDecimal("offer_stipend")); } catch (SQLException ignored) {}
        try { d.setOfferStatus(rs.getString("offer_status")); } catch (SQLException ignored) {}

        return d;
    }
}
