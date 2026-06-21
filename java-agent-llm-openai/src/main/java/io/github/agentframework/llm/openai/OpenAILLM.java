package io.github.agentframework.llm.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.agentframework.llm.ChatLLM;
import io.github.agentframework.llm.ChatMessage;
import io.github.agentframework.llm.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * OpenAI 兼容的 LLM 适配器。支持 GPT、国产大模型等所有 OpenAI 兼容 API。
 * 使用 HttpURLConnection 以兼容 JDK 8。
 */
public class OpenAILLM implements ChatLLM {

    private static final Logger log = LoggerFactory.getLogger(OpenAILLM.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutMs;

    public OpenAILLM(String apiKey) {
        this(apiKey, "gpt-4o-mini", "https://api.openai.com/v1", 30000);
    }

    public OpenAILLM(String apiKey, String model, String baseUrl, int timeoutMs) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        try {
            String json = buildChatRequest(messages);
            String response = postJson(baseUrl + "/chat/completions", json);
            return parseChatResponse(response);
        } catch (Exception e) {
            log.error("LLM call failed", e);
            return "Thought: 调用 LLM 失败\nAnswer: 抱歉，我暂时无法处理，错误：" + e.getMessage();
        }
    }

    @Override
    public void chatStream(List<ChatMessage> messages, Consumer<String> receiver) {
        try {
            String json = buildStreamRequest(messages);
            postJsonStream(baseUrl + "/chat/completions", json, receiver);
        } catch (Exception e) {
            log.error("LLM stream call failed", e);
        }
    }

    private String buildChatRequest(List<ChatMessage> messages) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);

        ArrayNode msgArray = root.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = msgArray.addObject();
            msgNode.put("role", roleToString(msg.getRole()));
            msgNode.put("content", msg.getContent());
        }

        return MAPPER.writeValueAsString(root);
    }

    private String buildStreamRequest(List<ChatMessage> messages) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        root.put("stream", true);
        ArrayNode msgArray = root.putArray("messages");
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = msgArray.addObject();
            msgNode.put("role", roleToString(msg.getRole()));
            msgNode.put("content", msg.getContent());
        }
        return MAPPER.writeValueAsString(root);
    }

    private void postJsonStream(String urlStr, String json, Consumer<String> receiver) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(0);
            conn.setDoOutput(true);
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));
            OutputStream os = conn.getOutputStream();
            try { os.write(body); os.flush(); } finally { os.close(); }
            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) break;
                        try {
                            JsonNode node = MAPPER.readTree(data);
                            JsonNode delta = node.get("choices").get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                receiver.accept(delta.get("content").asText());
                            }
                        } catch (Exception ignored) {}
                    }
                }
                reader.close();
            } else {
                throw new RuntimeException("LLM API error " + status + ": " + readStream(conn.getErrorStream()));
            }
        } finally { conn.disconnect(); }
    }

    private String roleToString(Role role) {
        switch (role) {
            case SYSTEM:   return "system";
            case USER:     return "user";
            case ASSISTANT: return "assistant";
            default:       return "user";
        }
    }

    private String parseChatResponse(String responseJson) throws Exception {
        JsonNode root = MAPPER.readTree(responseJson);
        JsonNode choice = root.get("choices").get(0);
        return choice.get("message").get("content").asText();
    }

    private String postJson(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));

            OutputStream os = conn.getOutputStream();
            try {
                os.write(body);
                os.flush();
            } finally {
                os.close();
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                return readStream(conn.getInputStream());
            } else {
                String error = readStream(conn.getErrorStream());
                throw new RuntimeException("LLM API error " + status + ": " + error);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
