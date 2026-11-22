package com.nlogistic.util;

import com.nlogistic.db_config.GetConnection;
import java.sql.*;

public class InspectDoc43 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = GetConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT document_id, file_path, original_file_name, stored_file_name, uploaded_at FROM documents ORDER BY document_id DESC LIMIT 10")) {
            System.out.println("=== Recent Documents ===");
            while (rs.next()) {
                System.out.println("DocID=" + rs.getLong("document_id") +
                                   " | Path=" + rs.getString("file_path") +
                                   " | OrigName=" + rs.getString("original_file_name") +
                                   " | StoredName=" + rs.getString("stored_file_name"));
            }
        }
    }
}
