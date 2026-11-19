package com.example.knowledge.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import com.example.knowledge.common.BadRequestException;

/**
 * Turns an uploaded PDF / DOCX / TXT file into plain-text sections.
 * PDFs are split per page so answers can cite page numbers.
 */
@Component
public class DocumentTextExtractor {

    /** A run of text and, for PDFs, the 1-based page it came from (null otherwise). */
    public record Section(String text, Integer page) {
    }

    public List<Section> extract(Path file, String fileType) throws IOException {
        return switch (fileType) {
            case "pdf" -> pdf(file);
            case "docx" -> docx(file);
            case "txt" -> txt(file);
            default -> throw new BadRequestException("Unsupported file type: " + fileType);
        };
    }

    private List<Section> pdf(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<Section> sections = new ArrayList<>();
            int pages = document.getNumberOfPages();
            for (int page = 1; page <= pages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document);
                if (text != null && !text.isBlank()) {
                    sections.add(new Section(text.strip(), page));
                }
            }
            return sections;
        }
    }

    private List<Section> docx(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            return text == null || text.isBlank() ? List.of() : List.of(new Section(text.strip(), null));
        }
    }

    private List<Section> txt(Path file) throws IOException {
        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1); // strip BOM
        }
        return text.isBlank() ? List.of() : List.of(new Section(text.strip(), null));
    }
}
