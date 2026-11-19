package com.example.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.knowledge.common.BadRequestException;
import com.example.knowledge.document.DocumentTextExtractor;
import com.example.knowledge.document.DocumentTextExtractor.Section;

class DocumentTextExtractorTest {

    private final DocumentTextExtractor extractor = new DocumentTextExtractor();

    @TempDir
    Path tmp;

    @Test
    void readsPlainText() throws IOException {
        Path file = tmp.resolve("a.txt");
        Files.writeString(file, "﻿Hello knowledge base", StandardCharsets.UTF_8);

        List<Section> sections = extractor.extract(file, "txt");

        assertThat(sections).hasSize(1);
        assertThat(sections.get(0).text()).isEqualTo("Hello knowledge base"); // BOM removed
        assertThat(sections.get(0).page()).isNull();
    }

    @Test
    void blankTextYieldsNoSections() throws IOException {
        Path file = tmp.resolve("blank.txt");
        Files.writeString(file, "   \n  ");
        assertThat(extractor.extract(file, "txt")).isEmpty();
    }

    @Test
    void readsDocx() throws IOException {
        Path file = tmp.resolve("a.docx");
        try (XWPFDocument doc = new XWPFDocument(); OutputStream out = Files.newOutputStream(file)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Annual leave is 24 days");
            doc.write(out);
        }

        List<Section> sections = extractor.extract(file, "docx");

        assertThat(sections).hasSize(1);
        assertThat(sections.get(0).text()).contains("Annual leave is 24 days");
    }

    @Test
    void readsPdfPerPageWithPageNumbers() throws IOException {
        Path file = tmp.resolve("a.pdf");
        try (PDDocument doc = new PDDocument()) {
            for (String text : List.of("First page text", "Second page text")) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(50, 700);
                    cs.showText(text);
                    cs.endText();
                }
            }
            doc.save(file.toFile());
        }

        List<Section> sections = extractor.extract(file, "pdf");

        assertThat(sections).hasSize(2);
        assertThat(sections.get(0).page()).isEqualTo(1);
        assertThat(sections.get(0).text()).contains("First page text");
        assertThat(sections.get(1).page()).isEqualTo(2);
        assertThat(sections.get(1).text()).contains("Second page text");
    }

    @Test
    void rejectsUnsupportedTypes() {
        assertThatThrownBy(() -> extractor.extract(tmp.resolve("x.exe"), "exe"))
                .isInstanceOf(BadRequestException.class);
    }
}
