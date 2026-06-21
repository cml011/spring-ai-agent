package io.github.agentframework.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "agent-framework")
public class AgentFrameworkProperties {

    private Llm llm = new Llm();
    private Plugin plugin = new Plugin();

    public Llm getLlm() { return llm; }
    public void setLlm(Llm llm) { this.llm = llm; }
    public Plugin getPlugin() { return plugin; }
    public void setPlugin(Plugin plugin) { this.plugin = plugin; }

    public static class Llm {
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        private String baseUrl = "https://api.openai.com/v1";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class Plugin {
        private Database database = new Database();
        private Knowledge knowledge = new Knowledge();

        public Database getDatabase() { return database; }
        public void setDatabase(Database database) { this.database = database; }
        public Knowledge getKnowledge() { return knowledge; }
        public void setKnowledge(Knowledge knowledge) { this.knowledge = knowledge; }

        public static class Knowledge {
            private boolean enabled = false;
            private String chunker = "simple";
            private String embedderApiKey = "";
            private String embedderModel = "text-embedding-3-small";
            private String embedderBaseUrl = "https://api.openai.com/v1";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public String getChunker() { return chunker; }
            public void setChunker(String chunker) { this.chunker = chunker; }
            public String getEmbedderApiKey() { return embedderApiKey; }
            public void setEmbedderApiKey(String key) { this.embedderApiKey = key; }
            public String getEmbedderModel() { return embedderModel; }
            public void setEmbedderModel(String model) { this.embedderModel = model; }
            public String getEmbedderBaseUrl() { return embedderBaseUrl; }
            public void setEmbedderBaseUrl(String url) { this.embedderBaseUrl = url; }
        }

        public static class Database {
            private boolean enabled = false;
            private boolean autoDiscover = false;
            private List<String> includeTables = new ArrayList<String>();
            private Map<String, String> columnDescriptions = new LinkedHashMap<String, String>();
            private boolean tenantEnabled = true;
            private String dialect = "h2";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            public boolean isAutoDiscover() { return autoDiscover; }
            public void setAutoDiscover(boolean autoDiscover) { this.autoDiscover = autoDiscover; }
            public List<String> getIncludeTables() { return includeTables; }
            public void setIncludeTables(List<String> includeTables) { this.includeTables = includeTables; }
            public Map<String, String> getColumnDescriptions() { return columnDescriptions; }
            public void setColumnDescriptions(Map<String, String> columnDescriptions) { this.columnDescriptions = columnDescriptions; }
            public boolean isTenantEnabled() { return tenantEnabled; }
            public void setTenantEnabled(boolean tenantEnabled) { this.tenantEnabled = tenantEnabled; }
            public String getDialect() { return dialect; }
            public void setDialect(String dialect) { this.dialect = dialect; }
        }
    }
}
