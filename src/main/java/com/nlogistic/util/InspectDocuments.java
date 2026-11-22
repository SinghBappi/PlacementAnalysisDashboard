package com.nlogistic.util;

import com.nlogistic.db_config.GetConnection;
import java.sql.*;

public class InspectDocuments {
    public static void main(String[] args) throws Exception {
        try (Connection conn = GetConnection.getConnection()) {
            System.out.println("=== DOCUMENTS FOR BAPPI (student_id = 6622) ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT document_id, student_id, document_type_id, original_file_name, semester, is_archived, version, file_size, uploaded_at " +
                    "FROM documents WHERE student_id = 6622 ORDER BY semester ASC, version DESC")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("Doc ID: %-5d | Sem: %-2s | Arch: %-5s | Ver: %-2d | File: %-20s | Uploaded: %s%n",
                            rs.getLong("document_id"), rs.getString("semester"),
                            rs.getBoolean("is_archived"), rs.getInt("version"),
                            rs.getString("original_file_name"), rs.getString("uploaded_at"));
                    }
                }
            }

            System.out.println("\n=== ACADEMIC RECORDS FOR BAPPI (student_id = 6622) ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT academic_record_id, student_id, semester, document_id, exam_roll_number, verified " +
                    "FROM student_academic_records WHERE student_id = 6622 ORDER BY semester ASC")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("Record ID: %-5d | Sem: %-2d | Doc ID: %-5s | ExamRoll: %-10s | Verified: %s%n",
                            rs.getLong("academic_record_id"), rs.getInt("semester"),
                            rs.getString("document_id"), rs.getString("exam_roll_number"),
                            rs.getString("verified"));
                    }
                }
            }

            System.out.println("\n=== ALL VERIFICATION RECORDS ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT verification_id, student_id, academic_record_id, document_id, semester, workflow_status, created_at " +
                    "FROM academic_document_verifications ORDER BY student_id ASC, semester ASC, verification_id DESC")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("Verif ID: %-5d | Student: %-5d | Sem: %-2d | Doc ID: %-5d | Rec ID: %-5d | Status: %-22s | Created: %s%n",
                            rs.getLong("verification_id"), rs.getLong("student_id"), rs.getInt("semester"),
                            rs.getLong("document_id"), rs.getLong("academic_record_id"),
                            rs.getString("workflow_status"), rs.getString("created_at"));
                    }
                }
            }
        }
    }
}
