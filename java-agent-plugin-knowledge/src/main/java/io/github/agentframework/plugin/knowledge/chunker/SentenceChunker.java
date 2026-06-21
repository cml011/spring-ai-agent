package io.github.agentframework.plugin.knowledge.chunker;

import io.github.agentframework.plugin.knowledge.Chunk;
import io.github.agentframework.plugin.knowledge.Chunker;
import io.github.agentframework.plugin.knowledge.Document;

import java.util.ArrayList;
import java.util.List;

public class SentenceChunker implements Chunker {

    private static final int SENTENCES_PER_CHUNK = 3;
    private static final int OVERLAP = 1;
    private int totalChunks = 0;

    @Override
    public List<Chunk> chunk(Document document) {
        List<String> sentences = splitSentences(document.getContent());
        List<Chunk> chunks = new ArrayList<Chunk>();
        int step = SENTENCES_PER_CHUNK - OVERLAP;
        int index = 0;

        for (int i = 0; i < sentences.size(); i += step) {
            int end = Math.min(i + SENTENCES_PER_CHUNK, sentences.size());
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                sb.append(sentences.get(j));
            }
            chunks.add(new Chunk(document.getId(), document.getName(), sb.toString().trim(), index++));
            if (end >= sentences.size()) break;
        }

        totalChunks += chunks.size();
        return chunks;
    }

    @Override
    public int size() { return totalChunks; }

    @Override
    public void reset() { totalChunks = 0; }

    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            current.append(c);
            if (isEnd(c)) {
                String s = current.toString().trim();
                if (!s.isEmpty()) sentences.add(s);
                current = new StringBuilder();
            }
        }
        String last = current.toString().trim();
        if (!last.isEmpty()) sentences.add(last);
        return sentences;
    }

    private boolean isEnd(char c) {
        return c == '\u3002' || c == '\uff01' || c == '\uff1f'
            || c == '.' || c == '!' || c == '?' || c == '\n';
    }
}
