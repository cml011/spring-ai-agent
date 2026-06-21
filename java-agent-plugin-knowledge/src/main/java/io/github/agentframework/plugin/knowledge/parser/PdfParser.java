package io.github.agentframework.plugin.knowledge.parser;

import io.github.agentframework.plugin.knowledge.Chunk;
import io.github.agentframework.plugin.knowledge.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class PdfParser implements DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(PdfParser.class);

    public static String extractText(InputStream input) {
        try {
            PDDocument doc = PDDocument.load(input);
            PDFTextStripper stripper = new PDFTextStripper();
            String result = stripper.getText(doc);
            doc.close();
            return result != null ? result.trim() : null;
        } catch (Exception e) {
            log.error("PDF parsing failed", e);
            return null;
        }
    }

    @Override
    public List<Chunk> parse(Document document) {
        return new TextParser().parse(document);
    }
}
