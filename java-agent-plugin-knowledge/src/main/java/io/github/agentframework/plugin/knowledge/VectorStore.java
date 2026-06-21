package io.github.agentframework.plugin.knowledge;

import java.util.List;
import java.util.Map;

public interface VectorStore {

    void add(String id, float[] vector, String text, Map<String, String> metadata);

    List<SearchResult> search(float[] queryVector, int topK);

    int size();

    class SearchResult {
        private final String id;
        private final float score;
        private final String text;
        private final Map<String, String> metadata;

        public SearchResult(String id, float score, String text, Map<String, String> metadata) {
            this.id = id; this.score = score; this.text = text; this.metadata = metadata;
        }

        public String getId() { return id; }
        public float getScore() { return score; }
        public String getText() { return text; }
        public Map<String, String> getMetadata() { return metadata; }
    }
}
