package com.nlogistic.dao;

import com.nlogistic.model.DocumentType;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface DocumentTypeDAO {
    List<DocumentType> findAll();
    List<DocumentType> findAll(Connection conn);
    Optional<DocumentType> findById(Integer documentTypeId);
    Optional<DocumentType> findById(Integer documentTypeId, Connection conn);
    Optional<DocumentType> findByName(String typeName);
    Optional<DocumentType> findByName(String typeName, Connection conn);
}
