package com.nlogistic.controller.placement;

import com.nlogistic.model.*;
import com.nlogistic.service.AuthorizationService;
import com.nlogistic.service.DocumentService;
import com.nlogistic.util.FileValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB
        maxFileSize = 1024 * 1024 * 15,       // 15 MB
        maxRequestSize = 1024 * 1024 * 30     // 30 MB
)
public class DocumentManagementAction extends HttpServlet {

    private final DocumentService documentService = new DocumentService();
    private final AuthorizationService authzService = new AuthorizationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        if (!authzService.isPlacementExecutive(user) && !authzService.isPlacementCoordinator(user)) {
            resp.sendRedirect(req.getContextPath() + "/auth/access-denied");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        // API endpoints for dynamic dependent dropdowns
        if ("/api/drives".equals(pathInfo)) {
            handleApiDrives(req, resp);
            return;
        }
        if ("/api/students".equals(pathInfo)) {
            handleApiStudents(req, resp);
            return;
        }
        if ("/download".equals(pathInfo) || "/downlad".equals(pathInfo)) {
            handleDownload(req, resp, user);
            return;
        }
        if ("/upload".equals(pathInfo)) {
            handleUploadPage(req, resp, user);
            return;
        }
        if ("/repository".equals(pathInfo)) {
            handleRepositoryPage(req, resp, user);
            return;
        }

        // Default: Document Management Dashboard
        handleDashboardPage(req, resp, user);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            sendError(req, resp, HttpServletResponse.SC_UNAUTHORIZED, "Session expired. Please log in.");
            return;
        }
        if (!authzService.isPlacementExecutive(user)) {
            sendError(req, resp, HttpServletResponse.SC_FORBIDDEN, "Access denied. Placement Executive role required.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        if ("/upload".equals(pathInfo) || "".equals(pathInfo) || "/".equals(pathInfo)) {
            handleUploadSubmit(req, resp, user);
            return;
        }

        sendError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + pathInfo);
    }

    private void handleDashboardPage(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        Map<String, Object> stats = documentService.getExecutiveDashboardStats();
        req.setAttribute("stats", stats);
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/views/placement/document-dashboard.jsp").forward(req, resp);
    }

    private void handleUploadPage(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        List<Company> companies = documentService.getAllCompanies();
        List<DocumentType> docTypes = documentService.getAllDocumentTypes();
        req.setAttribute("companies", companies);
        req.setAttribute("docTypes", docTypes);
        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/views/placement/upload-document.jsp").forward(req, resp);
    }

    private void handleRepositoryPage(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        Long companyId = parseLong(req.getParameter("companyId"));
        Long driveId = parseLong(req.getParameter("driveId"));
        Integer documentTypeId = parseInteger(req.getParameter("documentTypeId"));
        String search = req.getParameter("search");

        List<Document> documents = documentService.getCompanyDocuments(companyId, driveId, documentTypeId, search);
        List<Company> companies = documentService.getAllCompanies();
        List<DocumentType> docTypes = documentService.getAllDocumentTypes();
        List<PlacementDrive> drives = (companyId != null && companyId > 0)
                ? documentService.getDrivesByCompanyId(companyId) : List.of();

        req.setAttribute("documents", documents);
        req.setAttribute("companies", companies);
        req.setAttribute("docTypes", docTypes);
        req.setAttribute("drives", drives);
        req.setAttribute("selectedCompanyId", companyId);
        req.setAttribute("selectedDriveId", driveId);
        req.setAttribute("selectedDocTypeId", documentTypeId);
        req.setAttribute("searchKeyword", search != null ? search : "");
        req.setAttribute("user", user);

        req.getRequestDispatcher("/WEB-INF/views/placement/company-documents.jsp").forward(req, resp);
    }

    private void handleApiDrives(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long companyId = parseLong(req.getParameter("companyId"));
        List<PlacementDrive> drives = documentService.getDrivesByCompanyId(companyId);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < drives.size(); i++) {
            if (i > 0) sb.append(",");
            PlacementDrive d = drives.get(i);
            sb.append("{\"driveId\":").append(d.getDriveId())
              .append(",\"companyId\":").append(d.getCompanyId())
              .append(",\"jobTitle\":\"").append(escapeJson(d.getJobTitle())).append("\"")
              .append(",\"status\":\"").append(escapeJson(d.getStatus())).append("\"}");
        }
        sb.append("]");
        resp.getWriter().write(sb.toString());
    }

    private void handleApiStudents(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Long driveId = parseLong(req.getParameter("driveId"));
        List<StudentProfile> students = documentService.getStudentsForDrive(driveId);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            if (i > 0) sb.append(",");
            StudentProfile s = students.get(i);
            sb.append("{\"studentId\":").append(s.getStudentId())
              .append(",\"rollNumber\":\"").append(escapeJson(s.getRollNumber())).append("\"")
              .append(",\"fullName\":\"").append(escapeJson(s.getFullName())).append("\"")
              .append(",\"email\":\"").append(escapeJson(s.getEmail())).append("\"")
              .append(",\"departmentCode\":\"").append(escapeJson(s.getDepartmentCode())).append("\"}");
        }
        sb.append("]");
        resp.getWriter().write(sb.toString());
    }

    private void handleUploadSubmit(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        try {
            Long companyId = parseLong(req.getParameter("companyId"));
            Long driveId = parseLong(req.getParameter("driveId"));
            Integer documentTypeId = parseInteger(req.getParameter("documentTypeId"));
            Long studentId = parseLong(req.getParameter("studentId"));

            String offerType = req.getParameter("offerType");
            String ctcStr = req.getParameter("ctc");
            String stipendStr = req.getParameter("stipend");
            String offerDateStr = req.getParameter("offerDate");
            String joiningDateStr = req.getParameter("joiningDate");
            String status = req.getParameter("status");

            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() <= 0) {
                filePart = req.getPart("documentFile");
            }

            DocumentService.UploadRequest uploadReq = new DocumentService.UploadRequest();
            uploadReq.companyId = companyId;
            uploadReq.driveId = driveId;
            uploadReq.documentTypeId = documentTypeId;
            uploadReq.studentId = studentId;
            uploadReq.offerType = (offerType != null && !offerType.trim().isEmpty()) ? offerType.trim() : "FULL_TIME";

            if (ctcStr != null && !ctcStr.trim().isEmpty()) {
                uploadReq.ctc = new BigDecimal(ctcStr.trim());
            }
            if (stipendStr != null && !stipendStr.trim().isEmpty()) {
                uploadReq.stipend = new BigDecimal(stipendStr.trim());
            }
            if (offerDateStr != null && !offerDateStr.trim().isEmpty()) {
                uploadReq.offerDate = LocalDate.parse(offerDateStr.trim());
            }
            if (joiningDateStr != null && !joiningDateStr.trim().isEmpty()) {
                uploadReq.joiningDate = LocalDate.parse(joiningDateStr.trim());
            }
            uploadReq.status = (status != null && !status.trim().isEmpty()) ? status.trim() : "OFFERED";
            uploadReq.filePart = filePart;
            uploadReq.uploadedByUserId = user.getUserId();
            uploadReq.servletContext = getServletContext();
            uploadReq.httpRequest = req;

            Long docId = documentService.uploadDocument(uploadReq);

            if (isAjax) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":true,\"documentId\":" + docId + ",\"message\":\"Document uploaded and mapped successfully!\"}");
            } else {
                resp.sendRedirect(req.getContextPath() + "/app/executive/documents/repository?companyId=" + (companyId != null ? companyId : "") + "&uploadSuccess=true");
            }

        } catch (IllegalArgumentException e) {
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                handleUploadPage(req, resp, user);
            }
        } catch (Exception e) {
            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson("Upload processing error: " + e.getMessage()) + "\"}");
            } else {
                req.setAttribute("errorMessage", "Error uploading document: " + e.getMessage());
                handleUploadPage(req, resp, user);
            }
        }
    }

    private void handleDownload(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        Long docId = parseLong(req.getParameter("id"));
        if (docId == null || docId <= 0) {
            renderDownloadError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Document Parameter", "Missing or invalid document ID parameter in request URL.", null);
            return;
        }

        try {
            DocumentService.DownloadResource resource = documentService.downloadDocument(docId, user, getServletContext(), req);

            String encodedFileName = URLEncoder.encode(resource.getOriginalFileName(), StandardCharsets.UTF_8).replace("+", "%20");
            resp.setContentType(resource.getMimeType());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getOriginalFileName() + "\"; filename*=UTF-8''" + encodedFileName);
            resp.setHeader("Content-Length", String.valueOf(resource.getFileSize()));
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");

            try (InputStream in = Files.newInputStream(resource.getFilePath());
                 OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

        } catch (SecurityException e) {
            renderDownloadError(req, resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied", e.getMessage(), docId);
        } catch (java.util.NoSuchElementException e) {
            renderDownloadError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Document File Not Found", e.getMessage(), docId);
        } catch (IllegalArgumentException e) {
            renderDownloadError(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Request", e.getMessage(), docId);
        } catch (Exception e) {
            renderDownloadError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Download Processing Error", "Download failed: " + e.getMessage(), docId);
        }
    }

    private void renderDownloadError(HttpServletRequest req, HttpServletResponse resp, int statusCode, String title, String message, Long docId) throws ServletException, IOException {
        // Set OK status on response so Tomcat/browser error interception does not replace custom error UI
        resp.setStatus(HttpServletResponse.SC_OK);
        req.setAttribute("statusCode", statusCode);
        req.setAttribute("errorTitle", title);
        req.setAttribute("errorMessage", message);
        req.setAttribute("documentId", docId);

        if (docId != null && docId > 0) {
            documentService.getDocumentById(docId).ifPresent(d -> req.setAttribute("document", d));
        }

        req.getRequestDispatcher("/WEB-INF/views/placement/document-error.jsp").forward(req, resp);
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp, int code, String msg) throws IOException {
        if ("XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))) {
            resp.setStatus(code);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(msg) + "\"}");
        } else {
            resp.sendError(code, msg);
        }
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
