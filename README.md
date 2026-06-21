# Spring AI Agent

轻量级、Java 原生的 AI Agent 编排框架。

让传统 Java 项目（JDK 8+）无需复杂改造即可接入 AI Agent。查数据库、搜知识库、调 Service、解析文档，全通过自然语言交互，Agent 自己动态决策。

---

## 核心架构

用户输入 → OrchestratorAgent (ReAct 循环)
  ├─ DatabaseQueryTool       自然语言查数据库（自动写 SQL）
  ├─ KnowledgeSearchTool     知识库检索
  ├─ KnowledgeIngestTool     文档导入知识库
  ├─ @AgentTool 注解          调用现有 Service
  └─ DelegationTool          委托给子 Agent

知识库内部架构：

上传文件 → Chunker（切片策略）→ Embedder（向量化）→ VectorStore（检索）
               ├─ SimpleChunker     按段落切
               └─ SentenceChunker   按句子边界切，重叠窗口

---

## 模块

| 模块 | 说明 | JDK |
|---|---|---|
| java-agent-core | 核心抽象：Agent、Tool、LLM、Memory、注解 | 8+ |
| java-agent-llm-openai | LLM 适配器（OpenAI、DeepSeek 等兼容） | 8+ |
| java-agent-plugin-database | 数据库查询 Tool + SchemaInspector + 方言支持 | 8+ |
| java-agent-plugin-knowledge | 知识库：文档解析（PDF/DOCX/TXT）、切片、向量检索 | 8+ |
| java-agent-spring-boot-starter | 自动配置：扫描 @AgentTool、注入 Agent、插件发现 | Boot 2.7+ |
| examples | 完整可运行的 Spring Boot Demo（H2、DeepSeek） | 8+ |

---

## 快速开始

### 启动 Demo

```bash
cd spring-ai-agent
set OPENAI_API_KEY=sk-your-key
mvn spring-boot:run -pl examples
```

### 数据库自然语言查询

```bash
curl -X POST http://localhost:8080/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"帮我统计今年的销售额\"}"
```

### 知识库文档检索（预置了 4 份消防文档）

```bash
curl -X POST http://localhost:8080/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"巡检报告里提到哪些消防隐患？\"}"
```

### 上传文件到知识库

```bash
curl -X POST http://localhost:8080/api/upload -F "file=@报告.pdf"
curl -X POST http://localhost:8080/api/upload -F "file=@合同.docx"
```

### 流式输出（SSE，打字机效果）

```bash
curl -N http://localhost:8080/ai/chat/sse?message=帮我统计今年的销售额
```

---

## 文档解析能力

| 文件格式 | 解析方式 |
|---|---|
| .txt | 纯文本读取 |
| .docx | Apache POI 提取文字 |
| .pdf | Apache PDFBox 提取文字 |

---

## 配置参考

```yaml
agent-framework:
  llm:
    api-key: sk-your-key
    model: deepseek-v4-flash
    base-url: https://api.deepseek.com/v1

  plugin:
    database:
      enabled: true
      dialect: mysql
      include-tables:
        - orders
        - products

    knowledge:
      enabled: true
      chunker: simple
      embedder:
        api-key: your-embedding-key
        model: text-embedding-3-small
        base-url: https://api.openai.com/v1
```

---

## API 接口

| 端点 | 方法 | 说明 |
|---|---|---|
| /ai/chat | POST | 对话（JSON body: {message}） |
| /ai/chat/sse | GET | 流式对话（query param: message） |
| /api/upload | POST | 上传文件到知识库（multipart: file） |
| /knowledge/stats | GET | 知识库统计 |

---

## 集成到现有项目

```xml
<dependency>
    <groupId>io.github.agentframework</groupId>
    <artifactId>java-agent-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

给 Service 加 @AgentTool：

```java
@Service
public class ProjectService {
    @AgentTool(description = "创建维保项目，需要项目名称、大厦名称、年份、开始日期、结束日期、项目类型")
    public Project createProject(CreateProjectRequest request) {
        return projectMapper.insert(request);
    }
}
```

---

## 开发计划

- [x] ReAct Agent 核心
- [x] 数据库自然语言查询（H2 / MySQL 方言）
- [x] 知识库文档检索
- [x] @AgentTool 注解驱动
- [x] 租户隔离
- [x] PDF / DOCX / TXT 解析
- [x] 智能切片（句子边界 + 重叠窗口）
- [x] 向量搜索
- [x] 流式输出（SSE）
- [ ] OCR 识别（企业版）
- [ ] 图片多模态识别
- [ ] Agent 间通信
- [ ] 可视化编排

## License\n\nApache 2.0

## 联系作者

如需企业版咨询、技术合作或集成指导，欢迎联系。

Email: your-email@example.com
GitHub: https://github.com/your-username/spring-ai-agent


