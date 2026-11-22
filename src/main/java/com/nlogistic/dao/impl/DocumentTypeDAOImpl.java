package com.nlogistic.dao.impl;

import com.nlogistic.dao.DocumentTypeDAO;
import com.nlogistic.db_config.GetConnection;
import com.nlogistic.model.DocumentType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentTypeDAOImpl implements DocumentTypeDAO {

    private static final String SELECT_ALL = "SELECT document_type_id, type_name, description FROM document_types ORDER BY document_type_id";
    private static final String SELECT_BY_ID = "SELECT document_type_id, type_name, description FROM document_types WHERE document_type_id = ?";
    private static final String SELECT_BY_NAME = "SELECT document_type_id, type_name, description FROM document_types WHERE type_name = ?";

    @Override
    public List<DocumentType> findAll() {
        try (Connection conn = GetConnection.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all document types", e);
        }
    }

    @Override
    public List<DocumentType> findAll(Connection conn) {
        List<DocumentType> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll document types", e);
        }
        return list;
    }

    @Override
    public Optional<DocumentType> findById(Integer documentTypeId) {
        try (Connection conn = GetConnection.getConnection()) {
            return findById(documentTypeId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document type by ID: " + documentTypeId, e);
        }
    }

    @Override
    public Optional<DocumentType> findById(Integer documentTypeId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, documentTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document type by ID with connection", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DocumentType> findByName(String typeName) {
        try (Connection conn = GetConnection.getConnection()) {
            return findByName(typeName, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document type by name: " + typeName, e);
        }
    }

    @Override
    public Optional<DocumentType> findByName(String typeName, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, typeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding document type by name with connection", e);
        }
        return Optional.empty();
    }

    private DocumentType mapRow(ResultSet rs) throws SQLException {
        return new DocumentType(
                rs.getInt("document_type_id"),
                rs.getString("type_name"),
                rs.getString("description")
        );
    }
}
