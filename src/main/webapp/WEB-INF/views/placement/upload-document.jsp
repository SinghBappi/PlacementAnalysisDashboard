<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.nlogistic.model.Company" %>
<%@ page import="com.nlogistic.model.DocumentType" %>
<%@ page import="java.util.List" %>
<%@ include file="/WEB-INF/views/common/auth-guard.jspf" %>
<%
    List<Company> companies = (List<Company>) request.getAttribute("companies");
    List<DocumentType> docTypes = (List<DocumentType>) request.getAttribute("docTypes");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Placement Document - ThakurPlacer</title>
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
        .form-card {
            background: white;
            border-radius: 18px;
            border: none;
            box-shadow: var(--card-shadow);
        }
        .dropzone-box {
            border: 2px dashed #cbd5e1;
            border-radius: 14px;
            padding: 30px 20px;
            text-align: center;
            background-color: #f8fafc;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .dropzone-box:hover, .dropzone-box.dragover {
            border-color: #059669;
            background-color: #ecfdf5;
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
                        <i class="fa-solid fa-cloud-arrow-up text-success me-2"></i> Upload Placement Document
                    </h5>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <a href="${pageContext.request.contextPath}/app/executive/documents/repository" class="btn btn-outline-dark btn-sm rounded-pill px-3">
                        <i class="fa-solid fa-database me-1"></i> Repository
                    </a>
                </div>
            </div>

            <!-- Content Body -->
            <div class="content-body">
                <div class="row justify-content-center">
                    <div class="col-xl-9 col-lg-11">

                        <!-- Notifications -->
                        <div id="ajaxAlertContainer">
                            <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
                                <div class="alert alert-danger alert-dismissible fade show rounded-3" role="alert">
                                    <i class="fa-solid fa-triangle-exclamation me-2"></i> <%= errorMessage %>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                                </div>
                            <% } %>
                            <% if (successMessage != null && !successMessage.isEmpty()) { %>
                                <div class="alert alert-success alert-dismissible fade show rounded-3" role="alert">
                                    <i class="fa-solid fa-circle-check me-2"></i> <%= successMessage %>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                                </div>
                            <% } %>
                        </div>

                        <!-- Form Card -->
                        <div class="card form-card p-4 p-md-5">
                            <div class="border-bottom pb-3 mb-4">
                                <h4 class="fw-bold text-dark mb-1">Upload Placement Record or Offer Letter</h4>
                                <p class="text-muted small mb-0">Documents are automatically stored in structured folder hierarchies partitioned by Company and Placement Drive.</p>
                            </div>

                            <form id="uploadDocForm" method="POST" action="${pageContext.request.contextPath}/app/executive/documents/upload" enctype="multipart/form-data">

                                <div class="row g-3 mb-4">
                                    <!-- 1. Company Selection -->
                                    <div class="col-md-6">
                                        <label for="companySelect" class="form-label fw-semibold text-dark">
                                            1. Target Company <span class="text-danger">*</span>
                                        </label>
                                        <select class="form-select" id="companySelect" name="companyId" required>
                                            <option value="">-- Select Company --</option>
                                            <% if (companies != null) {
                                                for (Company c : companies) { %>
                                                    <option value="<%= c.getCompanyId() %>"><%= c.getCompanyName() %> (<%= c.getIndustry() %>)</option>
                                            <%   }
                                               } %>
                                        </select>
                                        <div class="form-text small">Select the corporate entity associated with this document.</div>
                                    </div>

                                    <!-- 2. Placement Drive Selection -->
                                    <div class="col-md-6">
                                        <label for="driveSelect" class="form-label fw-semibold text-dark">
                                            2. Placement Drive <span class="text-danger">*</span>
                                        </label>
                                        <select class="form-select" id="driveSelect" name="driveId" required disabled>
                                            <option value="">-- Select Company First --</option>
                                        </select>
                                        <div class="form-text small" id="driveHelpText">Drives will populate automatically upon company selection.</div>
                                    </div>
                                </div>

                                <div class="row g-3 mb-4">
                                    <!-- 3. Document Category -->
                                    <div class="col-md-6">
                                        <label for="categorySelect" class="form-label fw-semibold text-dark">
                                            3. Document Category <span class="text-danger">*</span>
                                        </label>
                                        <select class="form-select" id="categorySelect" name="documentTypeId" required>
                                            <option value="">-- Select Document Category --</option>
                                            <option value="6">Job Description (JD)</option>
                                            <option value="9">Shortlisted Candidate List</option>
                                            <option value="5">Final Selection List</option>
                                            <option value="4">Student Offer Letter</option>
                                            <option value="8">Other Placement Document</option>
                                        </select>
                                        <div class="form-text small">Determines storage subfolder and allowed file formats.</div>
                                    </div>

                                    <!-- Category description badge -->
                                    <div class="col-md-6 d-flex align-items-center">
                                        <div id="categoryHintBox" class="p-3 bg-light rounded-3 w-100 border text-muted small">
                                            <i class="fa-solid fa-circle-info text-primary me-1"></i>
                                            <span id="categoryHintText">Please choose a category to view format restrictions.</span>
                                        </div>
                                    </div>
                                </div>

                                <!-- 4. Offer Letter Mapping Section (Shown conditionally) -->
                                <div id="offerLetterFields" class="p-4 mb-4 rounded-3 border border-success-subtle bg-success-subtle bg-opacity-10 d-none">
                                    <div class="d-flex align-items-center gap-2 mb-3">
                                        <i class="fa-solid fa-award text-success fs-5"></i>
                                        <h6 class="fw-bold text-success mb-0">Official Offer Letter Details & Student Mapping</h6>
                                    </div>

                                    <div class="row g-3 mb-3">
                                        <div class="col-md-6">
                                            <label for="studentSelect" class="form-label fw-semibold text-dark">
                                                Select Student Candidate <span class="text-danger">*</span>
                                            </label>
                                            <select class="form-select" id="studentSelect" name="studentId">
                                                <option value="">-- Select Drive First --</option>
                                            </select>
                                            <div class="form-text small">Links the offer letter directly to the concerned student's profile.</div>
                                        </div>
                                        <div class="col-md-6">
                                            <label for="offerType" class="form-label fw-semibold text-dark">Offer Type</label>
                                            <select class="form-select" id="offerType" name="offerType">
                                                <option value="FULL_TIME" selected>Full Time</option>
                                                <option value="INTERNSHIP">Internship</option>
                                                <option value="PPO">Pre-Placement Offer (PPO)</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="row g-3">
                                        <div class="col-md-3">
                                            <label for="ctc" class="form-label fw-semibold text-dark">CTC (LPA)</label>
                                            <input type="number" step="0.01" min="0" class="form-control" id="ctc" name="ctc" placeholder="e.g. 7.50">
                                        </div>
                                        <div class="col-md-3">
                                            <label for="stipend" class="form-label fw-semibold text-dark">Stipend (Monthly)</label>
                                            <input type="number" step="0.01" min="0" class="form-control" id="stipend" name="stipend" placeholder="e.g. 25000">
                                        </div>
                                        <div class="col-md-3">
                                            <label for="offerDate" class="form-label fw-semibold text-dark">Offer Date</label>
                                            <input type="date" class="form-control" id="offerDate" name="offerDate">
                                        </div>
                                        <div class="col-md-3">
                                            <label for="joiningDate" class="form-label fw-semibold text-dark">Joining Date</label>
                                            <input type="date" class="form-control" id="joiningDate" name="joiningDate">
                                        </div>
                                    </div>
                                </div>

                                <!-- 5. File Upload Area -->
                                <div class="mb-4">
                                    <label class="form-label fw-semibold text-dark">
                                        4. Select Document File <span class="text-danger">*</span>
                                    </label>
                                    <div class="dropzone-box" id="dropZone" onclick="document.getElementById('fileInput').click()">
                                        <i class="fa-solid fa-cloud-arrow-up fs-1 text-success mb-2"></i>
                                        <h6 class="fw-bold text-dark mb-1">Click to browse or drag and drop your file here</h6>
                                        <p class="text-muted small mb-0" id="fileFormatGuidance">Max upload size: 10 MB. PDF required for Offer Letters.</p>
                                        <div id="selectedFileInfo" class="mt-2 fw-semibold text-success d-none">
                                            <i class="fa-solid fa-circle-check me-1"></i> <span id="selectedFileName"></span>
                                            (<span id="selectedFileSize"></span>)
                                        </div>
                                    </div>
                                    <input type="file" id="fileInput" name="file" class="d-none" required>
                                </div>

                                <!-- Action Buttons -->
                                <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                                    <a href="${pageContext.request.contextPath}/app/executive/documents" class="btn btn-outline-secondary rounded-pill px-4">
                                        Cancel
                                    </a>
                                    <button type="submit" id="submitBtn" class="btn btn-success rounded-pill px-5 fw-semibold shadow-sm">
                                        <span id="btnText"><i class="fa-solid fa-cloud-arrow-up me-1"></i> Upload & Save Document</span>
                                        <span id="btnSpinner" class="spinner-border spinner-border-sm d-none" role="status"></span>
                                    </button>
                                </div>

                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const contextPath = '${pageContext.request.contextPath}';
    const companySelect = document.getElementById('companySelect');
    const driveSelect = document.getElementById('driveSelect');
    const categorySelect = document.getElementById('categorySelect');
    const studentSelect = document.getElementById('studentSelect');
    const offerLetterFields = document.getElementById('offerLetterFields');
    const fileInput = document.getElementById('fileInput');
    const dropZone = document.getElementById('dropZone');
    const selectedFileInfo = document.getElementById('selectedFileInfo');
    const selectedFileName = document.getElementById('selectedFileName');
    const selectedFileSize = document.getElementById('selectedFileSize');
    const categoryHintText = document.getElementById('categoryHintText');
    const fileFormatGuidance = document.getElementById('fileFormatGuidance');
    const uploadForm = document.getElementById('uploadDocForm');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    const alertContainer = document.getElementById('ajaxAlertContainer');

    // 1. Company selection -> Fetch drives via AJAX
    companySelect.addEventListener('change', function() {
        const compId = this.value;
        driveSelect.innerHTML = '<option value="">-- Loading Placement Drives... --</option>';
        driveSelect.disabled = true;

        if (!compId) {
            driveSelect.innerHTML = '<option value="">-- Select Company First --</option>';
            return;
        }

        fetch(contextPath + '/app/executive/documents/api/drives?companyId=' + encodeURIComponent(compId))
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    driveSelect.innerHTML = '<option value="">-- Select Placement Drive --</option>';
                    data.forEach(d => {
                        const opt = document.createElement('option');
                        opt.value = d.driveId;
                        opt.textContent = d.jobTitle + ' (ID: ' + d.driveId + ')';
                        driveSelect.appendChild(opt);
                    });
                    driveSelect.disabled = false;
                } else {
                    driveSelect.innerHTML = '<option value="">-- No Drives Found For This Company --</option>';
                }
            })
            .catch(err => {
                driveSelect.innerHTML = '<option value="">-- Error Loading Drives --</option>';
            });
    });

    // 2. Drive selection -> Fetch students
    driveSelect.addEventListener('change', function() {
        loadStudents(this.value);
    });

    function loadStudents(driveId) {
        studentSelect.innerHTML = '<option value="">-- Loading Students... --</option>';
        const url = contextPath + '/app/executive/documents/api/students' + (driveId ? '?driveId=' + encodeURIComponent(driveId) : '');
        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    studentSelect.innerHTML = '<option value="">-- Select Candidate Student --</option>';
                    data.forEach(s => {
                        const opt = document.createElement('option');
                        opt.value = s.studentId;
                        opt.textContent = s.fullName + ' (' + s.rollNumber + ' - ' + s.departmentCode + ')';
                        studentSelect.appendChild(opt);
                    });
                } else {
                    studentSelect.innerHTML = '<option value="">-- No Registered Students Found --</option>';
                }
            })
            .catch(() => {
                studentSelect.innerHTML = '<option value="">-- Error Loading Students --</option>';
            });
    }

    // 3. Category change handler
    categorySelect.addEventListener('change', function() {
        const cat = this.value;
        if (cat === '4') { // OFFER_LETTER
            offerLetterFields.classList.remove('d-none');
            studentSelect.required = true;
            fileInput.accept = '.pdf,application/pdf';
            fileFormatGuidance.textContent = 'Official Offer Letters MUST be in PDF (.pdf) format. Max 10MB.';
            categoryHintText.innerHTML = '<strong class="text-success">Offer Letter:</strong> Saved under <code>uploads/.../OfferLetters/</code>. Mapped to selected student profile.';
            if (driveSelect.value) {
                loadStudents(driveSelect.value);
            } else {
                loadStudents('');
            }
        } else {
            offerLetterFields.classList.add('d-none');
            studentSelect.required = false;

            if (cat === '6') { // JOB_DESCRIPTION
                fileInput.accept = '.pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document';
                fileFormatGuidance.textContent = 'Accepted formats: PDF (.pdf) or Word (.docx). Max 10MB.';
                categoryHintText.innerHTML = '<strong class="text-primary">Job Description:</strong> Saved under <code>uploads/.../JD/</code>.';
            } else if (cat === '9') { // SHORTLIST
                fileInput.accept = '.pdf,.docx,.xlsx,.csv';
                fileFormatGuidance.textContent = 'Accepted formats: PDF (.pdf), Excel (.xlsx), Word (.docx), CSV. Max 10MB.';
                categoryHintText.innerHTML = '<strong class="text-warning">Shortlist:</strong> Saved under <code>uploads/.../Shortlist/</code>.';
            } else if (cat === '5') { // SELECTION_LIST
                fileInput.accept = '.pdf,.docx,.xlsx,.csv';
                fileFormatGuidance.textContent = 'Accepted formats: PDF (.pdf), Excel (.xlsx), Word (.docx), CSV. Max 10MB.';
                categoryHintText.innerHTML = '<strong class="text-info">Selection List:</strong> Saved under <code>uploads/.../Selection/</code>.';
            } else {
                fileInput.accept = '.pdf,.docx,.xlsx,.csv,.txt';
                fileFormatGuidance.textContent = 'Accepted formats: PDF, Word, Excel, CSV. Max 10MB.';
                categoryHintText.innerHTML = 'General placement document saved under <code>uploads/.../Other/</code>.';
            }
        }
    });

    // 4. File selection display and validation
    fileInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const file = this.files[0];
            const maxBytes = 10 * 1024 * 1024; // 10MB
            if (file.size > maxBytes) {
                showAlert('danger', 'File size exceeds 10 MB limit (' + (file.size / (1024 * 1024)).toFixed(2) + ' MB). Please choose a smaller file.');
                this.value = '';
                selectedFileInfo.classList.add('d-none');
                return;
            }

            const cat = categorySelect.value;
            const ext = '.' + file.name.split('.').pop().toLowerCase();

            if (cat === '4' && ext !== '.pdf') {
                showAlert('danger', 'Offer letters must strictly be in PDF (.pdf) format.');
                this.value = '';
                selectedFileInfo.classList.add('d-none');
                return;
            }

            selectedFileName.textContent = file.name;
            selectedFileSize.textContent = (file.size < 1024 * 1024)
                ? (file.size / 1024).toFixed(1) + ' KB'
                : (file.size / (1024 * 1024)).toFixed(2) + ' MB';
            selectedFileInfo.classList.remove('d-none');
        }
    });

    // 5. Drag and drop events
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, e => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });
    });
    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, e => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
        });
    });
    dropZone.addEventListener('drop', e => {
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            fileInput.files = e.dataTransfer.files;
            fileInput.dispatchEvent(new Event('change'));
        }
    });

    // 6. Form submission via AJAX
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();

        if (!fileInput.files || fileInput.files.length === 0) {
            showAlert('danger', 'Please select a document file to upload.');
            return;
        }

        const formData = new FormData(this);
        submitBtn.disabled = true;
        btnText.classList.add('d-none');
        btnSpinner.classList.remove('d-none');

        fetch(uploadForm.action, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            submitBtn.disabled = false;
            btnText.classList.remove('d-none');
            btnSpinner.classList.add('d-none');

            if (data.success) {
                showAlert('success', data.message || 'Document uploaded successfully! Redirecting to repository...');
                uploadForm.reset();
                selectedFileInfo.classList.add('d-none');
                setTimeout(() => {
                    window.location.href = contextPath + '/app/executive/documents/repository?companyId=' + encodeURIComponent(companySelect.value || '');
                }, 1200);
            } else {
                showAlert('danger', data.error || 'Upload failed. Please check your submission.');
            }
        })
        .catch(err => {
            submitBtn.disabled = false;
            btnText.classList.remove('d-none');
            btnSpinner.classList.add('d-none');
            showAlert('danger', 'Network or server error occurred during document upload.');
        });
    });

    function showAlert(type, message) {
        alertContainer.innerHTML = '<div class="alert alert-' + type + ' alert-dismissible fade show rounded-3" role="alert">' +
            '<i class="fa-solid ' + (type === 'success' ? 'fa-circle-check' : 'fa-triangle-exclamation') + ' me-2"></i>' +
            message +
            '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>' +
            '</div>';
        alertContainer.scrollIntoView({ behavior: 'smooth' });
    }
</script>
</body>
</html>
