package io.github.agentframework.plugin.knowledge;

import java.util.List;

/**
 * 知识库检索策略。支持关键词检索和向量检索。
 */
public interface Searcher {

    void add(Chunk chunk);

    List<ScoredResult> search(String query, int topK);

    int size();

    class ScoredResult {
        private final Chunk chunk;
        private final double score;
        public ScoredResult(Chunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
        public Chunk getChunk() { return chunk; }
        public double getScore() { return score; }
    }
}
