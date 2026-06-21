package io.github.agentframework.plugin.knowledge.store;

import io.github.agentframework.plugin.knowledge.Chunk;

import java.util.*;

/**
 * 基于关键词的倒排索引存储。支持中文（字符二元分词）和英文（按空格分词）。
 */
public class SimpleChunkStore {

    private final List<Chunk> chunks = new ArrayList<Chunk>();
    private final Map<String, Set<Integer>> index = new HashMap<String, Set<Integer>>();

    public void add(Chunk chunk) {
        int idx = chunks.size();
        chunks.add(chunk);
        Set<String> tokens = tokenize(chunk.getContent());
        for (String token : tokens) {
            Set<Integer> postings = index.get(token);
            if (postings == null) {
                postings = new HashSet<Integer>();
                index.put(token, postings);
            }
            postings.add(idx);
        }
    }

    public void addAll(List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            add(chunk);
        }
    }

    /**
     * 搜索相关段落，返回排序后的结果。
     */
    public List<ScoredResult> search(String query, int topK) {
        if (query == null || query.trim().isEmpty() || chunks.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        int totalChunks = chunks.size();
        Map<Integer, Double> scores = new HashMap<Integer, Double>();

        for (String token : queryTokens) {
            Set<Integer> postings = index.get(token);
            if (postings == null) continue;

            double idf = Math.log(1.0 + (totalChunks - postings.size() + 0.5) / (postings.size() + 0.5));

            for (int chunkIdx : postings) {
                Double current = scores.get(chunkIdx);
                if (current == null) current = 0.0;
                String content = chunks.get(chunkIdx).getContent();
                double tf = countOccurrences(content, token);
                scores.put(chunkIdx, current + tf * idf);
            }
        }

        List<Map.Entry<Integer, Double>> sorted = new ArrayList<Map.Entry<Integer, Double>>(scores.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });

        List<ScoredResult> results = new ArrayList<ScoredResult>();
        int limit = Math.min(topK, sorted.size());
        for (int i = 0; i < limit; i++) {
            int chunkIdx = sorted.get(i).getKey();
            results.add(new ScoredResult(chunks.get(chunkIdx), sorted.get(i).getValue()));
        }

        return results;
    }

    public int size() {
        return chunks.size();
    }

    Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<String>();
        if (text == null || text.isEmpty()) return tokens;

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                current.append(c);
            } else {
                if (current.length() > 0) {
                    String word = current.toString();
                    if (word.length() > 1) {
                        tokens.add(word.toLowerCase());
                    }
                    // 中文：拆成二元组
                    if (isChinese(word)) {
                        for (int j = 0; j < word.length() - 1; j++) {
                            tokens.add(word.substring(j, j + 2));
                        }
                    }
                    current = new StringBuilder();
                }
            }
        }
        if (current.length() > 0) {
            String word = current.toString();
            if (word.length() > 1) {
                tokens.add(word.toLowerCase());
            }
            if (isChinese(word)) {
                for (int j = 0; j < word.length() - 1; j++) {
                    tokens.add(word.substring(j, j + 2));
                }
            }
        }

        return tokens;
    }

    private boolean isChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 0x4E00 || c > 0x9FFF) return false;
        }
        return true;
    }

    private double countOccurrences(String text, String token) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return (double) count;
    }

    public static class ScoredResult {
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
