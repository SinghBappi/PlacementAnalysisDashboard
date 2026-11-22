<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nlogistic.model.Document" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="/WEB-INF/views/common/auth-guard.jspf" %>
<%
    Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
    List<Document> recentDocs = (stats != null) ? (List<Document>) stats.get("recentDocuments") : List.of();
    int totalDocs = (stats != null && stats.get("totalDocuments") != null) ? (Integer) stats.get("totalDocuments") : 0;
    int totalOffers = (stats != null && stats.get("totalOfferLetters") != null) ? (Integer) stats.get("totalOfferLetters") : 0;
    int totalCompanies = (stats != null && stats.get("totalCompaniesWithDocs") != null) ? (Integer) stats.get("totalCompaniesWithDocs") : 0;
    int totalDrives = (stats != null && stats.get("totalDrivesWithDocs") != null) ? (Integer) stats.get("totalDrivesWithDocs") : 0;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document & Offer Letter Management - ThakurPlacer</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --exec-gradient: linear-gradient(135deg, #0f766e 0%, #059669 100%);
            --sidebar-bg: #0f172a;
            --card-shadow: 0 10px 25px rgba(0,0,0,0.05);
            --primary-emerald: #059669;
        }
        body {
            background-color: #f1f5f9;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            min-height: 100vh;
        }
        .sidebar {
            background-color: var(--sidebar-bg);
            min-height: 100vh;
            color: #94a3b8;
        }
        .sidebar-brand {
            padding: 24px 20px;
            color: white;
            font-size: 1.2rem;
            font-weight: 700;
            border-bottom: 1px solid rgba(255,255,255,0.08);
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .sidebar-menu {
            list-style: none;
            padding: 15px 0;
            margin: 0;
        }
        .sidebar-item {
            margin: 4px 12px;
        }
        .sidebar-link {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            color: #94a3b8;
            text-decoration: none;
            border-radius: 10px;
            font-weight: 500;
            font-size: 0.92rem;
            transition: all 0.2s ease;
        }
        .sidebar-link:hover {
            color: #ffffff;
            background: rgba(255,255,255,0.06);
        }
        .sidebar-link.active {
            color: #ffffff;
            background: #059669;
            box-shadow: 0 4px 12px rgba(5,150,105,0.3);
        }
        .sidebar-link i {
            font-size: 1.1rem;
            width: 22px;
            text-align: center;
        }
        .top-navbar {
            background: white;
            padding: 16px 28px;
            border-bottom: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .content-body {
            padding: 28px;
        }
        .stat-card {
            background: white;
            border-radius: 16px;
            border: none;
            box-shadow: var(--card-shadow);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 15px 30px rgba(0,0,0,0.1);
        }
        .icon-box {
            width: 52px;
            height: 52px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
        }
        .welcome-hero {
            background: white;
            border-radius: 18px;
            padding: 28px;
            box-shadow: var(--card-shadow);
            border-left: 6px solid #059669;
        }
        .badge-pill {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.78rem;
            font-weight: 600;
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-lg-3 col-xl-2 sidebar d-none d-lg-block">
            <div class="sidebar-brand">
                <i class="fa-solid fa-graduation-cap text-success fs-4"></i>
                <span>ThakurPlacer</span>
            </div>
            <div class="px-3 pt-3 pb-2 text-uppercase text-secondary" style="font-size: 0.72rem; letter-spacing: 1px;">Executive Portal</div>
            <ul class="sidebar-menu">
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/dashboard" class="sidebar-link">
                        <i class="fa-solid fa-gauge-high"></i> Dashboard
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/corporate-onboarding" class="sidebar-link">
                        <i class="fa-solid fa-handshake"></i> Corporate Onboarding
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/notice-builder" class="sidebar-link">
                        <i class="fa-solid fa-bullhorn"></i> Notice Builder
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/placement-drives" class="sidebar-link">
                        <i class="fa-solid fa-calendar-check"></i> Placement Drives
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/documents" class="sidebar-link active">
                        <i class="fa-solid fa-folder-open"></i> Document Management
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/academic-verification" class="sidebar-link">
                        <i class="fa-solid fa-file-circle-check text-warning"></i> Academic Marksheets
                    </a>
                </li>
                <li class="sidebar-item">
                    <a href="${pageContext.request.contextPath}/app/executive/courses" class="sidebar-link">
                        <i class="fa-solid fa-book-open-reader"></i> Course Administration
                    </a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-lg-9 col-xl-10">
            <!-- Top Navbar -->
            <div class="top-navbar d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-3">
                    <h5 class="mb-0 fw-bold text-dark">
                        <i class="fa-solid fa-folder-tree text-success me-2"></i> Centralized Document & Offer Letter Module
                    </h5>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <div class="text-end d-none d-sm-block">
                        <div class="fw-bold text-dark">${user.fullName}</div>
                        <small class="text-muted"><i class="fa-solid fa-shield-halved text-success me-1"></i> ${user.role}</small>
                    </div>
                    <a href="${pageContext.request.contextPath}/auth/logout" class="btn btn-outline-danger btn-sm rounded-pill px-3">
                        <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
                    </a>
                </div>
            </div>

            <!-- Content Body -->
            <div class="content-body">
                <!-- Welcome Banner -->
                <div class="welcome-hero mb-4">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h3 class="fw-bold text-dark mb-1">Central Placement Document Repository</h3>
                            <p class="text-muted mb-0">Organize and manage company Job Descriptions, Shortlists, Selection Lists, and official Student Offer Letters.</p>
                        </div>
                        <div class="col-md-4 text-md-end mt-3 mt-md-0 d-flex gap-2 justify-content-md-end">
                            <a href="${pageContext.request.contextPath}/app/executive/documents/upload" class="btn btn-success rounded-pill px-4 fw-semibold shadow-sm">
                                <i class="fa-solid fa-cloud-arrow-up me-1"></i> Upload Document
                            </a>
                            <a href="${pageContext.request.contextPath}/app/executive/documents/repository" class="btn btn-outline-dark rounded-pill px-3 fw-semibold">
                                <i class="fa-solid fa-database me-1"></i> Browse Repository
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Real Metrics Row -->
                <div class="row g-4 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <div class="card stat-card p-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <div class="text-muted small fw-semibold text-uppercase">Total Documents</div>
                                    <h3 class="fw-bold mb-0 mt-1" id="statTotalDocs"><%= totalDocs %></h3>
                                </div>
                                <div class="icon-box bg-primary-subtle text-primary">
                                    <i class="fa-solid fa-file-shield"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card stat-card p-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <div class="text-muted small fw-semibold text-uppercase">Offer Letters</div>
                                    <h3 class="fw-bold mb-0 mt-1 text-success" id="statTotalOffers"><%= totalOffers %></h3>
                                </div>
                                <div class="icon-box bg-success-subtle text-success">
                                    <i class="fa-solid fa-award"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card stat-card p-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <div class="text-muted small fw-semibold text-uppercase">Companies with Docs</div>
                                    <h3 class="fw-bold mb-0 mt-1" id="statCompaniesWithDocs"><%= totalCompanies %></h3>
                                </div>
                                <div class="icon-box bg-info-subtle text-info">
                                    <i class="fa-solid fa-building"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card stat-card p-4">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <div class="text-muted small fw-semibold text-uppercase">Placement Drives</div>
                                    <h3 class="fw-bold mb-0 mt-1" id="statDrivesWithDocs"><%= totalDrives %></h3>
                                </div>
                                <div class="icon-box bg-warning-subtle text-warning">
                                    <i class="fa-solid fa-briefcase"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Uploads Table Card -->
                <div class="card stat-card p-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="fw-bold text-dark mb-0"><i class="fa-solid fa-clock-rotate-left text-success me-2"></i> Recently Uploaded Documents</h5>
                        <a href="${pageContext.request.contextPath}/app/executive/documents/repository" class="text-decoration-none small fw-semibold text-success">
                            View All in Repository <i class="fa-solid fa-arrow-right ms-1"></i>
                        </a>
                    </div>

                    <% if (recentDocs == null || recentDocs.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="fa-regular fa-folder-open fs-1 mb-2 d-block text-secondary"></i>
                            <h6 class="fw-bold text-dark">No documents uploaded yet</h6>
                            <p class="small mb-3">Upload company Job Descriptions, candidate shortlists, selection lists, or student offer letters to begin.</p>
                            <a href="${pageContext.request.contextPath}/app/executive/documents/upload" class="btn btn-sm btn-success rounded-pill px-3">
                                <i class="fa-solid fa-upload me-1"></i> Upload First Document
                            </a>
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light small text-uppercase text-secondary">
                                    <tr>
                                        <th>Document Name</th>
                                        <th>Category</th>
                                        <th>Company & Drive</th>
                                        <th>Associated Student</th>
                                        <th>Size</th>
                                        <th>Upload Date</th>
                                        <th>Uploaded By</th>
                                        <th class="text-center">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Document doc : recentDocs) { %>
                                        <tr>
                                            <td>
                                                <div class="d-flex align-items-center gap-2">
                                                    <% if (doc.getDocumentTypeId() == 4) { %>
                                                        <i class="fa-solid fa-file-pdf text-danger fs-5"></i>
                                                    <% } else if (doc.getOriginalFileName() != null && doc.getOriginalFileName().endsWith(".xlsx")) { %>
                                                        <i class="fa-solid fa-file-excel text-success fs-5"></i>
                                                    <% } else if (doc.getOriginalFileName() != null && doc.getOriginalFileName().endsWith(".docx")) { %>
                                                        <i class="fa-solid fa-file-word text-primary fs-5"></i>
                                                    <% } else { %>
                                                        <i class="fa-solid fa-file text-secondary fs-5"></i>
                                                    <% } %>
                                                    <span class="fw-semibold text-dark"><%= doc.getOriginalFileName() %></span>
                                                </div>
                                            </td>
                                            <td>
                                                <span class="badge <%= doc.getCategoryBadgeClass() %> badge-pill">
                                                    <%= (doc.getDocumentTypeName() != null) ? doc.getDocumentTypeName().replace("_", " ") : "DOCUMENT" %>
                                                </span>
                                            </td>
                                            <td>
                                                <div class="fw-semibold text-dark"><%= (doc.getCompanyName() != null) ? doc.getCompanyName() : "—" %></div>
                                                <small class="text-muted"><%= (doc.getDriveTitle() != null) ? doc.getDriveTitle() : "—" %></small>
                                            </td>
                                            <td>
                                                <% if (doc.getStudentName() != null) { %>
                                                    <div class="fw-semibold text-dark"><%= doc.getStudentName() %></div>
                                                    <small class="text-muted"><i class="fa-solid fa-id-card me-1"></i> <%= doc.getStudentRollNumber() %></small>
                                                <% } else { %>
                                                    <span class="text-muted">—</span>
                                                <% } %>
                                            </td>
                                            <td><small class="text-muted"><%= doc.getFormattedFileSize() %></small></td>
                                            <td><small class="text-muted"><%= doc.getFormattedUploadedAt() %></small></td>
                                            <td><small class="text-muted"><%= (doc.getUploadedByName() != null) ? doc.getUploadedByName() : "System" %></small></td>
                                            <td class="text-center">
                                                <a href="${pageContext.request.contextPath}/app/executive/documents/download?id=<%= doc.getDocumentId() %>" class="btn btn-sm btn-outline-primary rounded-pill px-3" title="Download Document">
                                                    <i class="fa-solid fa-download me-1"></i> Download
                                                </a>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
