package com.nlogistic.model;

import java.io.Serializable;

public class DocumentType implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer documentTypeId;
    private String typeName;
    private String description;

    public DocumentType() {}

    public DocumentType(Integer documentTypeId, String typeName, String description) {
        this.documentTypeId = documentTypeId;
        this.typeName = typeName;
        this.description = description;
    }

    public Integer getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Integer documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DocumentType{" +
                "documentTypeId=" + documentTypeId +
                ", typeName='" + typeName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
