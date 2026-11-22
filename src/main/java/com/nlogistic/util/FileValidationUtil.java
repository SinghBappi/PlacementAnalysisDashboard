package com.nlogistic.util;

import javax.servlet.http.Part;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class FileValidationUtil {

    public static final long DEFAULT_MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".sh", ".vbs", ".js", ".jsp", ".jspx", ".jar",
            ".war", ".ear", ".class", ".com", ".scr", ".msi", ".dll", ".so",
            ".ps1", ".php", ".py", ".pl", ".cgi", ".asp", ".aspx"
    );

    // Magic Bytes signatures
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04}; // PK\x03\x04 (used by .docx, .xlsx)
    private static final byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}; // .doc (OLE2 compound)
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG

    public static long getMaxUploadSizeBytes() {
        String sysProp = System.getProperty("thakurplacer.upload.max_size");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            try {
                return Long.parseLong(sysProp.trim());
            } catch (NumberFormatException ignored) {}
        }
        String env = System.getenv("THAKURPLACER_UPLOAD_MAX_SIZE");
        if (env != null && !env.trim().isEmpty()) {
            try {
                return Long.parseLong(env.trim());
            } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_MAX_FILE_SIZE;
    }

    /**
     * Sanitizes original file name, stripping path elements (directory traversal protection)
     * and null characters.
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unnamed_document";
        }
        String clean = fileName.replace('\\', '/');
        int slash = clean.lastIndexOf('/');
        if (slash >= 0) {
            clean = clean.substring(slash + 1);
        }
        clean = clean.replaceAll("[\\p{Cntrl}]", "");
        clean = clean.replace("..", "");
        clean = clean.trim();
        if (clean.isEmpty()) {
            return "unnamed_document";
        }
        return clean;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot).toLowerCase(Locale.ROOT);
        }
        return "";
    }

    /**
     * Comprehensive validation of uploaded file Part based on document category ID:
     *  4 = OFFER_LETTER (Strictly PDF only)
     *  6 = JOB_DESCRIPTION (PDF, DOCX)
     *  5 = SELECTION_LIST (PDF, XLSX, DOCX, CSV)
     *  9 = SHORTLIST (PDF, XLSX, DOCX, CSV)
     *  8 / other = OTHER (PDF, XLSX, DOCX, CSV)
     */
    public static void validateFile(Part part, Integer documentTypeId) {
        if (part == null || part.getSize() <= 0) {
            throw new IllegalArgumentException("Upload rejected: File is empty or missing.");
        }

        long maxSize = getMaxUploadSizeBytes();
        if (part.getSize() > maxSize) {
            throw new IllegalArgumentException(String.format("Upload rejected: File size (%.2f MB) exceeds maximum allowed limit of %.2f MB.",
                    part.getSize() / (1024.0 * 1024.0), maxSize / (1024.0 * 1024.0)));
        }

        String rawName = part.getSubmittedFileName();
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Upload rejected: File name cannot be empty.");
        }

        if (rawName.contains("..") || rawName.contains("/") || rawName.contains("\\")) {
            String sanitized = sanitizeFileName(rawName);
            if (sanitized.isEmpty() || sanitized.equals("unnamed_document")) {
                throw new IllegalArgumentException("Upload rejected: Dangerous path traversal pattern detected in file name.");
            }
        }

        String sanitizedName = sanitizeFileName(rawName);
        String ext = getFileExtension(sanitizedName);

        if (DANGEROUS_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Upload rejected: Executable or script files are strictly prohibited.");
        }

        int typeId = documentTypeId != null ? documentTypeId : 8;

        if (typeId == 4) {
            if (!".pdf".equals(ext)) {
                throw new IllegalArgumentException("Upload rejected: Offer letters must be in PDF format only (.pdf).");
            }
            validatePdfMagicBytes(part);
        } else if (typeId == 6) {
            if (!".pdf".equals(ext) && !".docx".equals(ext) && !".doc".equals(ext)) {
                throw new IllegalArgumentException("Upload rejected: Job Descriptions must be PDF or Word documents (.pdf, .docx, .doc).");
            }
            if (".pdf".equals(ext)) {
                validatePdfMagicBytes(part);
            } else if (".docx".equals(ext)) {
                validateZipMagicBytes(part);
            } else if (".doc".equals(ext)) {
                validateOleMagicBytes(part);
            }
        } else if (typeId == 5 || typeId == 9) {
            if (!".pdf".equals(ext) && !".xlsx".equals(ext) && !".docx".equals(ext) && !".csv".equals(ext)) {
                throw new IllegalArgumentException("Upload rejected: Selection/Shortlist documents must be in PDF, Excel, Word, or CSV format.");
            }
            if (".pdf".equals(ext)) {
                validatePdfMagicBytes(part);
            } else if (".docx".equals(ext) || ".xlsx".equals(ext)) {
                validateZipMagicBytes(part);
            } else if (".csv".equals(ext)) {
                validateTextFile(part);
            }
        } else {
            Set<String> allowed = Set.of(".pdf", ".docx", ".doc", ".xlsx", ".csv", ".txt", ".jpg", ".jpeg", ".png");
            if (!allowed.contains(ext)) {
                throw new IllegalArgumentException("Upload rejected: Allowed document formats are PDF, DOCX, XLSX, CSV, and images.");
            }
            if (".pdf".equals(ext)) {
                validatePdfMagicBytes(part);
            } else if (".docx".equals(ext) || ".xlsx".equals(ext)) {
                validateZipMagicBytes(part);
            } else if (".doc".equals(ext)) {
                validateOleMagicBytes(part);
            } else if (".png".equals(ext)) {
                validatePngMagicBytes(part);
            } else if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
                validateJpegMagicBytes(part);
            } else if (".csv".equals(ext) || ".txt".equals(ext)) {
                validateTextFile(part);
            }
        }
    }

    public static void validateOleMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[4];
            int read = in.read(header);
            if (read < 4 || !Arrays.equals(header, OLE_MAGIC)) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid Word (.doc) document.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    public static void validatePngMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[4];
            int read = in.read(header);
            if (read < 4 || !Arrays.equals(header, PNG_MAGIC)) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid PNG image.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    public static void validateJpegMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[3];
            int read = in.read(header);
            if (read < 3 || !Arrays.equals(header, JPEG_MAGIC)) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid JPEG image.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    public static void validatePdfMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[4];
            int read = in.read(header);
            if (read < 4 || !Arrays.equals(header, PDF_MAGIC)) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid PDF document (magic-byte check failed).");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    public static void validateZipMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[4];
            int read = in.read(header);
            if (read < 4 || !Arrays.equals(header, ZIP_MAGIC)) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid Office XML document.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    public static void validateTextFile(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] sample = new byte[512];
            int read = in.read(sample);
            for (int i = 0; i < read; i++) {
                if (sample[i] == 0) {
                    throw new IllegalArgumentException("Upload rejected: Binary content detected in text/CSV file.");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to inspect text file content.", e);
        }
    }

    /**
     * Inspects magic bytes for WEBP (RIFF....WEBP).
     */
    public static void validateWebpMagicBytes(Part part) {
        try (InputStream in = new BufferedInputStream(part.getInputStream())) {
            byte[] header = new byte[12];
            int read = in.read(header);
            if (read < 12) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is too short to be a valid WEBP image.");
            }
            if (header[0] != 0x52 || header[1] != 0x49 || header[2] != 0x46 || header[3] != 0x46) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. Missing RIFF container header for WEBP image.");
            }
            if (header[8] != 0x57 || header[9] != 0x45 || header[10] != 0x42 || header[11] != 0x50) {
                throw new IllegalArgumentException("Upload rejected: File content validation failed. The file is not a valid WEBP image.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload rejected: Unable to read file content for validation.", e);
        }
    }

    /**
     * Comprehensive validation for student profile photos.
     * Enforces:
     * - Non-null, non-empty Part
     * - Size <= 2 MB
     * - Safe filename, no path traversal, no dangerous/executable extensions
     * - Allowed extensions: .jpg, .jpeg, .png, .webp
     * - Magic-bytes inspection
     * - Image decode validation (ImageIO) for JPG/PNG
     */
    public static void validateProfilePhoto(Part part) {
        if (part == null || part.getSize() <= 0) {
            throw new IllegalArgumentException("Upload rejected: Profile photo file is empty or missing.");
        }

        long maxPhotoSize = 2L * 1024 * 1024; // 2 MB
        if (part.getSize() > maxPhotoSize) {
            throw new IllegalArgumentException(String.format(
                    "Upload rejected: Profile photo size (%.2f MB) exceeds maximum allowed limit of 2.00 MB.",
                    part.getSize() / (1024.0 * 1024.0)));
        }

        String rawName = part.getSubmittedFileName();
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Upload rejected: Photo file name cannot be empty.");
        }

        if (rawName.contains("..") || rawName.contains("/") || rawName.contains("\\")) {
            String sanitized = sanitizeFileName(rawName);
            if (sanitized.isEmpty() || sanitized.equals("unnamed_document")) {
                throw new IllegalArgumentException("Upload rejected: Dangerous path traversal pattern detected in file name.");
            }
        }

        String sanitizedName = sanitizeFileName(rawName);
        String ext = getFileExtension(sanitizedName);

        if (DANGEROUS_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Upload rejected: Executable or script files are strictly prohibited.");
        }

        Set<String> allowedPhotoExts = Set.of(".jpg", ".jpeg", ".png", ".webp");
        if (!allowedPhotoExts.contains(ext)) {
            throw new IllegalArgumentException("Upload rejected: Only JPG, JPEG, PNG, and WEBP image formats are supported for profile photos.");
        }

        // Verify magic bytes
        if (".png".equals(ext)) {
            validatePngMagicBytes(part);
        } else if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
            validateJpegMagicBytes(part);
        } else if (".webp".equals(ext)) {
            validateWebpMagicBytes(part);
        }

        // For standard formats (JPG, PNG), verify image raster decode via ImageIO to reject corrupted or disguised executables
        if (".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext)) {
            try (InputStream in = new BufferedInputStream(part.getInputStream())) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(in);
                if (img == null) {
                    throw new IllegalArgumentException("Upload rejected: Invalid image data. File header contains image signature but raster data cannot be decoded.");
                }
                if (img.getWidth() < 10 || img.getHeight() < 10) {
                    throw new IllegalArgumentException("Upload rejected: Image dimensions too small (minimum 10x10 pixels required).");
                }
                if (img.getWidth() > 5000 || img.getHeight() > 5000) {
                    throw new IllegalArgumentException("Upload rejected: Image dimensions exceed maximum permitted size of 5000x5000 pixels.");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Upload rejected: Unable to read image content for structural validation.", e);
            }
        }
    }
}
