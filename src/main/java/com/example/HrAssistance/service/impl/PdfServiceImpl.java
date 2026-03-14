package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.service.PdfService;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;              // ← loads the PDF
import org.apache.pdfbox.pdmodel.PDDocument;  // ← the document object
import org.apache.pdfbox.text.PDFTextStripper; // ← extracts textimport org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    public String extractText(MultipartFile file) {

        // Null checks
        if (file == null || file.isEmpty()) {
            log.error("❌ File is null or empty");
            return null;
        }

        if (!isPdf(file)) {
            log.error("❌ File is not a PDF: {}", file.getContentType());
            return null;
        }

        try {
            PDDocument document = Loader.loadPDF(file.getBytes());

            if (document.getNumberOfPages() == 0) {
                log.error("❌ PDF has no pages");
                document.close();
                return null;
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            if (text == null || text.trim().isEmpty()) {
                log.error("❌ No text extracted — PDF might be image-based");
                return null;
            }

            log.info("Extracted {} characters from PDF", text.length());
            return text;

        } catch (Exception e) {
            log.error("PDF extraction failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
}