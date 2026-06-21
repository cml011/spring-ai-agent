package io.github.agentframework.plugin.knowledge.parser;

import io.github.agentframework.plugin.knowledge.Chunk;
import io.github.agentframework.plugin.knowledge.Document;
import java.util.List;

/**
 * 文档解析器接口。将 Document 拆分为 Chunk 列表。
 */
public interface DocumentParser {

    List<Chunk> parse(Document document);
}
