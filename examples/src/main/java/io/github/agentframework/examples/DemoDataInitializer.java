package io.github.agentframework.examples;

import io.github.agentframework.plugin.knowledge.Document;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    @Autowired(required = false)
    private KnowledgeBase knowledgeBase;

    @Override
    public void run(String... args) throws Exception {
        if (knowledgeBase == null) return;

        InputStream is = getClass().getClassLoader().getResourceAsStream("knowledge-samples.txt");
        if (is == null) {
            log.warn("knowledge-samples.txt not found");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder content = new StringBuilder();
        String currentName = null;
        String line;
        int count = 0;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("=== ") && line.endsWith(" ===")) {
                if (currentName != null && content.length() > 0) {
                    knowledgeBase.ingest(new Document(currentName, "txt", content.toString().trim()));
                    count++;
                }
                currentName = line.substring(4, line.length() - 4);
                content = new StringBuilder();
            } else {
                content.append(line).append("\n");
            }
        }
        if (currentName != null && content.length() > 0) {
            knowledgeBase.ingest(new Document(currentName, "txt", content.toString().trim()));
            count++;
        }
        reader.close();

        log.info("Loaded {} sample documents into knowledge base ({} chunks)", count, knowledgeBase.getChunkCount());
    }
}
