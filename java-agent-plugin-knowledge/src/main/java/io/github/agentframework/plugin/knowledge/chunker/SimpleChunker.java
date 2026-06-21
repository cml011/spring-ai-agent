package io.github.agentframework.plugin.knowledge.chunker;

import io.github.agentframework.plugin.knowledge.Chunk;
import io.github.agentframework.plugin.knowledge.Chunker;
import io.github.agentframework.plugin.knowledge.Document;

import java.util.ArrayList;
import java.util.List;

public class SimpleChunker implements Chunker {

    private static final int MAX_CHUNK_SIZE = 500;
    private int totalChunks = 0;

    @Override
    public List<Chunk> chunk(Document document) {
        List<Chunk> chunks = new ArrayList<Chunk>();
        String content = document.getContent();
        if (content == null || content.trim().isEmpty()) return chunks;

        String[] paragraphs = content.split("\\n\\n+|\\r\\n\\r\\n+");
        StringBuilder current = new StringBuilder();
        int index = 0;

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            if (current.length() + trimmed.length() > MAX_CHUNK_SIZE && current.length() > 0) {
                chunks.add(new Chunk(document.getId(), document.getName(), current.toString().trim(), index++));
                current = new StringBuilder();
            }
            if (current.length() > 0) current.append("\n\n");
            current.append(trimmed);
        }
        if (current.length() > 0) {
            chunks.add(new Chunk(document.getId(), document.getName(), current.toString().trim(), index));
        }
        totalChunks += chunks.size();
        return chunks;
    }

    @Override
    public int size() { return totalChunks; }

    @Override
    public void reset() { totalChunks = 0; }
}
