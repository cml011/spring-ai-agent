package io.github.agentframework.examples;

import io.github.agentframework.plugin.knowledge.Document;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import io.github.agentframework.plugin.knowledge.parser.PdfParser;
import io.github.agentframework.plugin.knowledge.parser.WordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired(required = false)
    private KnowledgeBase knowledgeBase;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<String, Object>();
        String filename = file.getOriginalFilename();
        result.put("name", filename);
        result.put("size", file.getSize());

        if (knowledgeBase == null) {
            result.put("error", "知识库未启用");
            return result;
        }

        try {
            String text = extractText(file);
            if (text == null || text.trim().isEmpty()) {
                result.put("error", "未能从文件中提取到文本内容");
                return result;
            }

            Document doc = new Document(filename, "txt", text);
            knowledgeBase.ingest(doc);

            String preview = text.length() > 300 ? text.substring(0, 300) + "..." : text;
            result.put("content", preview);
            result.put("totalLength", text.length());
            result.put("chunks", knowledgeBase.getChunkCount());
            result.put("message", "文件已导入知识库，可直接提问文件内容");

        } catch (Exception e) {
            log.error("File processing failed", e);
            result.put("error", "处理失败: " + e.getMessage());
        }

        return result;
    }

    private String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) return null;
        String lower = filename.toLowerCase();

        if (lower.endsWith(".pdf")) {
            return PdfParser.extractText(file.getInputStream());
        } else if (lower.endsWith(".docx")) {
            return WordParser.extractText(file.getInputStream());
        } else if (lower.endsWith(".txt")) {
            return readText(file.getInputStream());
        } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "[图片文件，目前不支持识别]";
        } else {
            return "[不支持的文件格式: " + filename + "]";
        }
    }

    private String readText(InputStream input) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line).append("\n");
        }
        r.close();
        return sb.toString();
    }
}
