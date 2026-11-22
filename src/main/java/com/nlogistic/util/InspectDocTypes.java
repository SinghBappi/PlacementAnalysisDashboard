package com.nlogistic.util;

import com.nlogistic.db_config.GetConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectDocTypes {
    public static void main(String[] args) {
        try (Connection conn = GetConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT document_type_id, type_name, description FROM document_types")) {
            System.out.println("=== document_types ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
