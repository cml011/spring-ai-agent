package io.github.agentframework.plugin.knowledge;

import io.github.agentframework.plugin.knowledge.chunker.SimpleChunker;
import io.github.agentframework.plugin.knowledge.embedding.Embedder;
import io.github.agentframework.plugin.knowledge.store.SimpleChunkStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeBase {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBase.class);

    private final Chunker chunker;
    private final Embedder embedder;
    private final VectorStore vectorStore;
    private final SimpleChunkStore fallbackStore;

    public KnowledgeBase() {
        this.chunker = new SimpleChunker();
        this.embedder = null;
        this.vectorStore = null;
        this.fallbackStore = new SimpleChunkStore();
    }

    public KnowledgeBase(Chunker chunker) {
        this(chunker, null, null);
    }

    public KnowledgeBase(Chunker chunker, Embedder embedder, VectorStore vectorStore) {
        this.chunker = chunker;
        this.embedder = embedder;
        this.vectorStore = vectorStore;
        this.fallbackStore = (embedder == null) ? new SimpleChunkStore() : null;
    }

    public void ingest(Document document) {
        List<Chunk> chunks = chunker.chunk(document);
        if (embedder != null && vectorStore != null) {
            for (Chunk chunk : chunks) {
                float[] vec = embedder.embed(chunk.getContent());
                if (vec != null) {
                    Map<String, String> meta = new HashMap<>();
                    meta.put("docName", document.getName());
                    vectorStore.add(chunk.getDocId() + "_" + chunk.getIndex(), vec, chunk.getContent(), meta);
                }
            }
            log.info("Ingested: {} ({} chunks, vector)", document.getName(), chunks.size());
        } else if (fallbackStore != null) {
            for (Chunk chunk : chunks) {
                fallbackStore.add(chunk);
            }
            log.info("Ingested: {} ({} chunks, keyword)", document.getName(), chunks.size());
        }
    }

    public void ingestAll(List<Document> documents) {
        for (Document doc : documents) ingest(doc);
    }

    public String search(String query, int topK) {
        if (embedder != null && vectorStore != null) {
            float[] queryVec = embedder.embed(query);
            if (queryVec == null) return "\uff08\u5411\u91cf\u5316\u5931\u8d25\uff09";
            List<VectorStore.SearchResult> results = vectorStore.search(queryVec, topK);
            if (results.isEmpty()) return "\uff08\u77e5\u8bc6\u5e93\u4e2d\u6ca1\u6709\u627e\u5230\u76f8\u5173\u4fe1\u606f\uff09";
            StringBuilder sb = new StringBuilder();
            sb.append("\u77e5\u8bc6\u5e93\u68c0\u7d22\u7ed3\u679c\uff1a\n\n");
            for (VectorStore.SearchResult r : results) {
                String name = (r.getMetadata() != null) ? r.getMetadata().get("docName") : "\u672a\u77e5";
                sb.append("- \u6765\u6e90\uff1a").append(name).append("\n");
                sb.append(r.getText()).append("\n\n");
            }
            return sb.toString().trim();
        } else if (fallbackStore != null) {
            List<SimpleChunkStore.ScoredResult> results = fallbackStore.search(query, topK);
            if (results.isEmpty()) return "\uff08\u77e5\u8bc6\u5e93\u4e2d\u6ca1\u6709\u627e\u5230\u76f8\u5173\u4fe1\u606f\uff09";
            StringBuilder sb = new StringBuilder();
            sb.append("\u77e5\u8bc6\u5e93\u68c0\u7d22\u7ed3\u679c\uff1a\n\n");
            for (SimpleChunkStore.ScoredResult r : results) {
                sb.append("- \u6765\u6e90\uff1a").append(r.getChunk().getDocName()).append("\n");
                sb.append(r.getChunk().getContent()).append("\n\n");
            }
            return sb.toString().trim();
        }
        return "\uff08\u77e5\u8bc6\u5e93\u672a\u5c31\u7eea\uff09";
    }

    public int getChunkCount() {
        if (vectorStore != null) return vectorStore.size();
        if (fallbackStore != null) return fallbackStore.size();
        return 0;
    }
}
