package io.github.agentframework.plugin.knowledge;

/**
 * 文档分块，包含文本内容和来源文档信息。
 */
public class Chunk {

    private final String docId;
    private final String docName;
    private final String content;
    private final int index;

    public Chunk(String docId, String docName, String content, int index) {
        this.docId = docId;
        this.docName = docName;
        this.content = content;
        this.index = index;
    }

    public String getDocId() { return docId; }
    public String getDocName() { return docName; }
    public String getContent() { return content; }
    public int getIndex() { return index; }
}
