package io.github.agentframework.plugin.knowledge.store;

import io.github.agentframework.plugin.knowledge.VectorStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MemoryVectorStore implements VectorStore {

    private final List<Entry> entries = new ArrayList<Entry>();

    private static class Entry {
        String id;
        float[] vector;
        String text;
        Map<String, String> metadata;
    }

    @Override
    public void add(String id, float[] vector, String text, Map<String, String> metadata) {
        Entry e = new Entry();
        e.id = id;
        e.vector = vector;
        e.text = text;
        e.metadata = metadata;
        entries.add(e);
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int topK) {
        if (entries.isEmpty() || queryVector == null) {
            return Collections.emptyList();
        }

        List<ScoredEntry> scored = new ArrayList<ScoredEntry>();
        for (Entry e : entries) {
            float sim = cosineSimilarity(queryVector, e.vector);
            scored.add(new ScoredEntry(e, sim));
        }

        Collections.sort(scored, new Comparator<ScoredEntry>() {
            @Override
            public int compare(ScoredEntry a, ScoredEntry b) {
                return Float.compare(b.score, a.score);
            }
        });

        List<SearchResult> results = new ArrayList<SearchResult>();
        int limit = Math.min(topK, scored.size());
        for (int i = 0; i < limit; i++) {
            ScoredEntry se = scored.get(i);
            results.add(new SearchResult(se.entry.id, se.score, se.entry.text, se.entry.metadata));
        }
        return results;
    }

    @Override
    public int size() {
        return entries.size();
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private static class ScoredEntry {
        final Entry entry;
        final float score;
        ScoredEntry(Entry e, float s) { entry = e; score = s; }
    }
}
