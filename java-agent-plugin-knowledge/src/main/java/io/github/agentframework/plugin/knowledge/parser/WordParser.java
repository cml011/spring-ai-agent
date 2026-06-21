package io.github.agentframework.plugin.knowledge.parser;

import io.github.agentframework.plugin.knowledge.Chunk;
import io.github.agentframework.plugin.knowledge.Document;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class WordParser implements DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(WordParser.class);

    public static String extractText(InputStream input) {
        try {
            XWPFDocument doc = new XWPFDocument(input);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            extractor.close();
            doc.close();
            return text;
        } catch (Exception e) {
            log.error("DOCX parsing failed", e);
            return null;
        }
    }

    @Override
    public List<Chunk> parse(Document document) {
        return new TextParser().parse(document);
    }
}
