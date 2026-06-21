package io.github.agentframework.boot;

import io.github.agentframework.agent.Agent;
import io.github.agentframework.agent.OrchestratorAgent;
import io.github.agentframework.agent.SystemPromptContributor;
import io.github.agentframework.llm.ChatLLM;
import io.github.agentframework.llm.openai.OpenAILLM;
import io.github.agentframework.plugin.database.DatabaseQueryTool;
import io.github.agentframework.plugin.database.SchemaInspector;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import io.github.agentframework.plugin.knowledge.tool.KnowledgeSearchTool;
import io.github.agentframework.plugin.knowledge.tool.KnowledgeIngestTool;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableConfigurationProperties(AgentFrameworkProperties.class)
public class AgentFrameworkAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AgentFrameworkAutoConfiguration.class);

    private final AgentFrameworkProperties properties;

    public AgentFrameworkAutoConfiguration(AgentFrameworkProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Bean
    public AgentToolBeanPostProcessor agentToolBeanPostProcessor(ToolRegistry toolRegistry) {
        return new AgentToolBeanPostProcessor(toolRegistry);
    }

    @Bean
    public ToolBeanPostProcessor toolBeanPostProcessor(ToolRegistry toolRegistry) {
        return new ToolBeanPostProcessor(toolRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatLLM chatLLM() {
        AgentFrameworkProperties.Llm llmProps = properties.getLlm();
        return new OpenAILLM(
                llmProps.getApiKey(),
                llmProps.getModel(),
                llmProps.getBaseUrl(),
                30000
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "agent")
    public Agent agent(ChatLLM llm, ToolRegistry toolRegistry,
                        List<Tool> tools,
                        List<SystemPromptContributor> contributors) {
        for (Tool tool : tools) {
            if (!toolRegistry.contains(tool.getName())) {
                toolRegistry.register(tool);
            }
        }
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("你是一个智能助手，帮助用户解答问题、处理业务。");

        for (SystemPromptContributor contributor : contributors) {
            String part = contributor.contribute();
            if (part != null && !part.isEmpty()) {
                systemPrompt.append("\n\n").append(part);
            }
        }

        return new OrchestratorAgent(
                "assistant",
                "全能助手，可以解答用户问题并调用各类工具",
                systemPrompt.toString(),
                llm,
                toolRegistry,
                Collections.<Agent>emptyList()
        );
    }

    @Configuration
    @ConditionalOnProperty(prefix = "agent-framework.plugin.database", name = "enabled", havingValue = "true")
    public static class DatabasePluginConfiguration {

        @Bean
        public SchemaInspector schemaInspector(DataSource dataSource, AgentFrameworkProperties props) {
            AgentFrameworkProperties.Plugin.Database db = props.getPlugin().getDatabase();
            return new SchemaInspector(
                    dataSource,
                    db.getIncludeTables(),
                    db.isAutoDiscover(),
                    db.getDialect()
            );
        }

        @Bean
        public Tool databaseQueryTool(DataSource dataSource, AgentFrameworkProperties props) {
            boolean tenantEnabled = props.getPlugin().getDatabase().isTenantEnabled();
            Tool tool = new DatabaseQueryTool(dataSource, tenantEnabled);
            log.info("Database query tool created");
            return tool;
        }

        @Bean
        public SystemPromptContributor databaseSchemaContributor(SchemaInspector schemaInspector) {
            return new SystemPromptContributor() {
                @Override
                public String contribute() {
                    return schemaInspector.inspect();
                }
            };
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "agent-framework.plugin.knowledge", name = "enabled", havingValue = "true")
    public static class KnowledgePluginConfiguration {

        @Bean
        public KnowledgeBase knowledgeBase(AgentFrameworkProperties props) {
            String chunkerType = props.getPlugin().getKnowledge().getChunker();
            io.github.agentframework.plugin.knowledge.Chunker chunker;
            if ("sentence".equals(chunkerType)) {
                chunker = new io.github.agentframework.plugin.knowledge.chunker.SentenceChunker();
            } else {
                chunker = new io.github.agentframework.plugin.knowledge.chunker.SimpleChunker();
            }
            String apiKey = props.getPlugin().getKnowledge().getEmbedderApiKey();
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                String model = props.getPlugin().getKnowledge().getEmbedderModel();
                String baseUrl = props.getPlugin().getKnowledge().getEmbedderBaseUrl();
                io.github.agentframework.plugin.knowledge.embedding.Embedder emb =
                    new io.github.agentframework.plugin.knowledge.embedding.ApiEmbedder(apiKey, model, baseUrl);
                return new KnowledgeBase(chunker, emb, new io.github.agentframework.plugin.knowledge.store.MemoryVectorStore());
            }
            return new KnowledgeBase(chunker);
        }

        @Bean
        public Tool knowledgeSearchTool(KnowledgeBase kb) {
            return new KnowledgeSearchTool(kb);
        }

        @Bean
        public Tool knowledgeIngestTool(KnowledgeBase kb) {
            return new KnowledgeIngestTool(kb);
        }

        public SystemPromptContributor knowledgeContributor() {
            return new SystemPromptContributor() {
                @Override
                public String contribute() {
                    return "????? searchKnowledgeBase ????????????\n?????? ingestDocument ???????????";
                }
            };
        }
    }
}
