<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nlogistic.model.Document" %>
<%@ page import="com.nlogistic.model.User" %>
<%
    Integer statusCode = (Integer) request.getAttribute("statusCode");
    if (statusCode == null) statusCode = 404;
    String errorTitle = (String) request.getAttribute("errorTitle");
    if (errorTitle == null) errorTitle = "Document Unavailable";
    String errorMessage = (String) request.getAttribute("errorMessage");
    if (errorMessage == null) errorMessage = "The requested document could not be retrieved.";
    Long docId = (Long) request.getAttribute("documentId");
    Document doc = (Document) request.getAttribute("document");

    User user = (User) session.getAttribute("user");
    boolean isExecutive = (user != null && (user.getRoleId() == 2 || user.getRoleId() == 3));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= errorTitle %> - ThakurPlacer Documents</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            color: #334155;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }
        .error-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 15px 35px rgba(15, 23, 42, 0.08);
            border: 1px solid #e2e8f0;
            max-width: 680px;
            width: 100%;
            overflow: hidden;
        }
        .error-header {
            background: linear-gradient(135deg, #0f766e 0%, #059669 100%);
            color: white;
            padding: 24px 30px;
            display: flex;
            align-items: center;
            gap: 15px;
        }
        .error-body {
            padding: 32px 30px;
        }
        .badge-status {
            font-size: 0.85rem;
            padding: 6px 12px;
            border-radius: 20px;
            font-weight: 600;
            background: rgba(255,255,255,0.2);
            color: #ffffff;
        }
        .details-box {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 16px 20px;
            margin: 20px 0;
            font-size: 0.92rem;
        }
        .details-row {
            display: flex;
            margin-bottom: 8px;
        }
        .details-row:last-child {
            margin-bottom: 0;
        }
        .details-label {
            width: 140px;
            font-weight: 600;
            color: #64748b;
            flex-shrink: 0;
        }
        .details-value {
            color: #0f172a;
            font-weight: 500;
            word-break: break-all;
        }
        .action-btns {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 24px;
        }
        .btn-emerald {
            background-color: #059669;
            border-color: #059669;
            color: white;
            font-weight: 600;
            border-radius: 8px;
            padding: 10px 20px;
            transition: all 0.2s;
        }
        .btn-emerald:hover {
            background-color: #047857;
            border-color: #047857;
            color: white;
            box-shadow: 0 4px 12px rgba(5, 150, 105, 0.3);
        }
    </style>
</head>
<body>

<div class="error-card">
    <div class="error-header">
        <div>
            <div class="d-flex align-items-center gap-2 mb-1">
                <i class="fa-solid fa-graduation-cap"></i>
                <span class="fw-bold tracking-wide">Thakur College of Science and Commerce</span>
            </div>
            <h4 class="mb-0 fw-bold">Placement Document Management</h4>
        </div>
        <span class="ms-auto badge-status">HTTP <%= statusCode %></span>
    </div>

    <div class="error-body">
        <div class="d-flex align-items-center gap-3 mb-3">
            <div class="rounded-circle p-3 <%= statusCode == 403 ? "bg-warning bg-opacity-10 text-warning" : "bg-danger bg-opacity-10 text-danger" %>">
                <i class="fa-solid <%= statusCode == 403 ? "fa-shield-halved" : "fa-file-circle-xmark" %> fa-2x"></i>
            </div>
            <div>
                <h5 class="fw-bold text-dark mb-1"><%= errorTitle %></h5>
                <p class="text-muted mb-0 small">The requested placement document could not be served by the storage system.</p>
            </div>
        </div>

        <div class="alert alert-<%= statusCode == 403 ? "warning" : "danger" %> d-flex align-items-start gap-2 py-3 px-3 my-3">
            <i class="fa-solid fa-circle-exclamation mt-1"></i>
            <div>
                <strong>Notice:</strong> <%= errorMessage %>
            </div>
        </div>

        <div class="details-box">
            <div class="details-row">
                <span class="details-label">Document ID:</span>
                <span class="details-value"><%= (docId != null && docId > 0) ? "#" + docId : "Not specified" %></span>
            </div>
            <% if (doc != null) { %>
                <div class="details-row">
                    <span class="details-label">File Name:</span>
                    <span class="details-value"><%= doc.getOriginalFileName() %></span>
                </div>
                <div class="details-row">
                    <span class="details-label">Category:</span>
                    <span class="details-value"><%= (doc.getDocumentTypeName() != null) ? doc.getDocumentTypeName() : ("Type ID: " + doc.getDocumentTypeId()) %></span>
                </div>
                <% if (doc.getCompanyName() != null) { %>
                    <div class="details-row">
                        <span class="details-label">Company:</span>
                        <span class="details-value"><%= doc.getCompanyName() %></span>
                    </div>
                <% } %>
                <% if (doc.getFormattedUploadedAt() != null) { %>
                    <div class="details-row">
                        <span class="details-label">Uploaded Date:</span>
                        <span class="details-value"><%= doc.getFormattedUploadedAt() %></span>
                    </div>
                <% } %>
            <% } %>
            <div class="details-row">
                <span class="details-label">Explanation:</span>
                <span class="details-value">
                    <% if (statusCode == 404) { %>
                        The database record is present, but the physical file is not found on server storage. This document may have been created during earlier test builds or removed from the server filesystem.
                    <% } else if (statusCode == 403) { %>
                        Security policy prevents access. Students may only download their own verified offer letters, and staff access requires placement cell authorization.
                    <% } else { %>
                        An internal storage or verification error occurred while processing the download request.
                    <% } %>
                </span>
            </div>
        </div>

        <% if (isExecutive && statusCode == 404) { %>
            <div class="p-3 bg-light rounded-3 border mb-3 small text-muted">
                <i class="fa-solid fa-lightbulb text-warning me-1"></i>
                <strong>Resolution Tip for Placement Staff:</strong>
                If this document is an official Job Description or Selection List, you can re-upload the current PDF file via the
                <a href="${pageContext.request.contextPath}/app/executive/documents/upload" class="fw-bold text-success text-decoration-none">Upload Document Form</a>.
            </div>
        <% } %>

        <div class="action-btns">
            <% if (isExecutive) { %>
                <a href="${pageContext.request.contextPath}/app/executive/documents/repository" class="btn btn-emerald">
                    <i class="fa-solid fa-folder-open me-2"></i>Back to Repository
                </a>
                <a href="${pageContext.request.contextPath}/app/executive/documents" class="btn btn-outline-secondary">
                    <i class="fa-solid fa-chart-pie me-2"></i>Document Dashboard
                </a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/app/student/offers" class="btn btn-emerald">
                    <i class="fa-solid fa-envelope-open-text me-2"></i>My Offer Letters
                </a>
                <a href="${pageContext.request.contextPath}/app/student/dashboard" class="btn btn-outline-secondary">
                    <i class="fa-solid fa-house me-2"></i>Dashboard
                </a>
            <% } %>
            <button onclick="window.history.back()" class="btn btn-light border ms-auto">
                <i class="fa-solid fa-arrow-left me-1"></i>Go Back
            </button>
        </div>
    </div>
</div>

</body>
</html>
