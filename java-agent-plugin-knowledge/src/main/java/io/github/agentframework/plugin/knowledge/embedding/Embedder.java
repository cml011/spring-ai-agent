package io.github.agentframework.plugin.knowledge.embedding;

import java.util.List;

/**
 * 文本向量化接口。将文本转为向量用于语义搜索。
 */
public interface Embedder {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);

    int dimension();
}
