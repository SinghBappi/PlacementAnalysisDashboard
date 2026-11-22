<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nlogistic.model.Document" %>
<%@ page import="com.nlogistic.model.Company" %>
<%@ page import="com.nlogistic.model.PlacementDrive" %>
<%@ page import="com.nlogistic.model.DocumentType" %>
<%@ page import="java.util.List" %>
<%@ include file="/WEB-INF/views/common/auth-guard.jspf" %>
<%
    List<Document> documents = (List<Document>) request.getAttribute("documents");
    List<Company> companies = (List<Company>) request.getAttribute("companies");
    List<PlacementDrive> drives = (List<PlacementDrive>) request.getAttribute("drives");
    List<DocumentType> docTypes = (List<DocumentType>) request.getAttribute("docTypes");

    Long selectedCompanyId = (Long) request.getAttribute("selectedCompanyId");
    Long selectedDriveId = (Long) request.getAttribute("selectedDriveId");
    Integer selectedDocTypeId = (Integer) request.getAttribute("selectedDocTypeId");
    String searchKeyword = (String) request.getAttribute("searchKeyword");
    if (searchKeyword == null) searchKeyword = "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Company Document Repository - ThakurPlacer</title>
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
        .filter-card {
            background: white;
            border-radius: 16px;
            border: none;
            box-shadow: var(--card-shadow);
        }
        .doc-table-card {
            background: white;
            border-radius: 16px;
            border: none;
            box-shadow: var(--card-shadow);
        }
        .badge-pill {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.78rem;
            font-weight: 600;
        }
        .nav-pills .nav-link {
            color: #64748b;
            font-weight: 500;
            border-radius: 30px;
            padding: 8px 18px;
        }
        .nav-pills .nav-link.active {
            background-color: #059669;
            color: white;
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
                    <a href="${pageContext.request.contextPath}/app/executive/documents" class="btn btn-outline-secondary btn-sm rounded-circle p-2" title="Back to Dashboard">
                        <i class="fa-solid fa-arrow-left"></i>
                    </a>
                    <h5 class="mb-0 fw-bold text-dark">
                        <i class="fa-solid fa-folder-open text-success me-2"></i> Company Document Repository
                    </h5>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <a href="${pageContext.request.contextPath}/app/executive/documents/upload" class="btn btn-success btn-sm rounded-pill px-3 shadow-sm">
                        <i class="fa-solid fa-cloud-arrow-up me-1"></i> Upload Document
                    </a>
                </div>
            </div>

            <!-- Content Body -->
            <div class="content-body">

                <!-- Filter Bar Card -->
                <div class="card filter-card p-4 mb-4">
                    <form method="GET" action="${pageContext.request.contextPath}/app/executive/documents/repository" class="row g-3 align-items-end">

                        <!-- Company Filter -->
                        <div class="col-md-3">
                            <label class="form-label small fw-semibold text-secondary">Company</label>
                            <select class="form-select form-select-sm" name="companyId" id="repCompanySelect">
                                <option value="">All Companies</option>
                                <% if (companies != null) {
                                    for (Company c : companies) {
                                        boolean selected = (selectedCompanyId != null && selectedCompanyId.equals(c.getCompanyId()));
                                %>
                                    <option value="<%= c.getCompanyId() %>" <%= selected ? "selected" : "" %>><%= c.getCompanyName() %></option>
                                <%  }
                                   } %>
                            </select>
                        </div>

                        <!-- Drive Filter -->
                        <div class="col-md-3">
                            <label class="form-label small fw-semibold text-secondary">Placement Drive</label>
                            <select class="form-select form-select-sm" name="driveId" id="repDriveSelect">
                                <option value="">All Drives</option>
                                <% if (drives != null) {
                                    for (PlacementDrive d : drives) {
                                        boolean selected = (selectedDriveId != null && selectedDriveId.equals(d.getDriveId()));
                                %>
                                    <option value="<%= d.getDriveId() %>" <%= selected ? "selected" : "" %>><%= d.getJobTitle() %></option>
                                <%  }
                                   } %>
                            </select>
                        </div>

                        <!-- Category Filter -->
                        <div class="col-md-2">
                            <label class="form-label small fw-semibold text-secondary">Category</label>
                            <select class="form-select form-select-sm" name="documentTypeId">
                                <option value="">All Categories</option>
                                <option value="6" <%= (selectedDocTypeId != null && selectedDocTypeId == 6) ? "selected" : "" %>>Job Descriptions (JD)</option>
                                <option value="9" <%= (selectedDocTypeId != null && selectedDocTypeId == 9) ? "selected" : "" %>>Shortlists</option>
                                <option value="5" <%= (selectedDocTypeId != null && selectedDocTypeId == 5) ? "selected" : "" %>>Selection Lists</option>
                                <option value="4" <%= (selectedDocTypeId != null && selectedDocTypeId == 4) ? "selected" : "" %>>Offer Letters</option>
                                <option value="8" <%= (selectedDocTypeId != null && selectedDocTypeId == 8) ? "selected" : "" %>>Other</option>
                            </select>
                        </div>

                        <!-- Search Input -->
                        <div class="col-md-2">
                            <label class="form-label small fw-semibold text-secondary">Search</label>
                            <div class="input-group input-group-sm">
                                <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
                                <input type="text" class="form-control" name="search" placeholder="File or student name..." value="<%= searchKeyword %>">
                            </div>
                        </div>

                        <!-- Buttons -->
                        <div class="col-md-2 d-flex gap-2">
                            <button type="submit" class="btn btn-sm btn-success rounded-pill px-3 flex-grow-1">
                                <i class="fa-solid fa-filter me-1"></i> Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/app/executive/documents/repository" class="btn btn-sm btn-outline-secondary rounded-pill px-3" title="Clear Filters">
                                Reset
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Document Results Table -->
                <div class="card doc-table-card p-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h5 class="fw-bold text-dark mb-0"><i class="fa-solid fa-folder-tree text-success me-2"></i> Stored Placement Documents</h5>
                            <small class="text-muted">Showing <%= (documents != null) ? documents.size() : 0 %> document records</small>
                        </div>
                    </div>

                    <% if (documents == null || documents.isEmpty()) { %>
                        <div class="text-center py-5 text-muted">
                            <i class="fa-regular fa-folder-open fs-1 mb-2 d-block text-secondary"></i>
                            <h6 class="fw-bold text-dark">No documents found</h6>
                            <p class="small mb-3">No files matched your filter criteria or no documents have been uploaded for the selected company/drive yet.</p>
                            <a href="${pageContext.request.contextPath}/app/executive/documents/upload" class="btn btn-sm btn-success rounded-pill px-3">
                                <i class="fa-solid fa-upload me-1"></i> Upload Document Now
                            </a>
                        </div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light small text-uppercase text-secondary">
                                    <tr>
                                        <th>File Name</th>
                                        <th>Category</th>
                                        <th>Company & Drive</th>
                                        <th>Student Mapping</th>
                                        <th>Size</th>
                                        <th>Uploaded On</th>
                                        <th>Uploaded By</th>
                                        <th class="text-center">Download</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Document doc : documents) { %>
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
                                                    <div>
                                                        <div class="fw-semibold text-dark"><%= doc.getOriginalFileName() %></div>
                                                        <small class="text-muted" style="font-size: 0.72rem;">ID: #<%= doc.getDocumentId() %></small>
                                                    </div>
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
                                                    <% if (doc.getOfferCtc() != null) { %>
                                                        <div class="small text-success fw-bold">₹<%= doc.getOfferCtc().stripTrailingZeros().toPlainString() %> LPA</div>
                                                    <% } %>
                                                <% } else { %>
                                                    <span class="text-muted">—</span>
                                                <% } %>
                                            </td>
                                            <td><small class="text-muted"><%= doc.getFormattedFileSize() %></small></td>
                                            <td><small class="text-muted"><%= doc.getFormattedUploadedAt() %></small></td>
                                            <td><small class="text-muted"><%= (doc.getUploadedByName() != null) ? doc.getUploadedByName() : "System" %></small></td>
                                            <td class="text-center">
                                                <a href="${pageContext.request.contextPath}/app/executive/documents/download?id=<%= doc.getDocumentId() %>" class="btn btn-sm btn-outline-primary rounded-pill px-3" title="Download <%= doc.getOriginalFileName() %>">
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
<script>
    const contextPath = '${pageContext.request.contextPath}';
    const repCompanySelect = document.getElementById('repCompanySelect');
    const repDriveSelect = document.getElementById('repDriveSelect');

    repCompanySelect.addEventListener('change', function() {
        const compId = this.value;
        repDriveSelect.innerHTML = '<option value="">All Drives</option>';
        if (!compId) return;

        fetch(contextPath + '/app/executive/documents/api/drives?companyId=' + encodeURIComponent(compId))
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    data.forEach(d => {
                        const opt = document.createElement('option');
                        opt.value = d.driveId;
                        opt.textContent = d.jobTitle;
                        repDriveSelect.appendChild(opt);
                    });
                }
            })
            .catch(() => {});
    });
</script>
</body>
</html>
