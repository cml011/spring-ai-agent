package io.github.agentframework.plugin.knowledge;

import java.util.List;

/**
 * 文档切片策略。将 Document 拆分为 Chunk 列表。
 */
public interface Chunker {

    List<Chunk> chunk(Document document);

    int size();

    default void reset() {}
}
