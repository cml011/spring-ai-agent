package io.github.agentframework.plugin.knowledge.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApiEmbedder implements Embedder {

    private static final Logger log = LoggerFactory.getLogger(ApiEmbedder.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public ApiEmbedder(String apiKey) {
        this(apiKey, "text-embedding-3-small", "https://api.openai.com/v1");
    }

    public ApiEmbedder(String apiKey, String model, String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public float[] embed(String text) {
        try {
            List<float[]> results = embedBatch(java.util.Collections.singletonList(text));
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Embedding failed", e);
            return null;
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<float[]>();
        try {
            String json = buildRequest(texts);
            String response = postJson(baseUrl + "/embeddings", json);
            JsonNode root = MAPPER.readTree(response);
            JsonNode data = root.get("data");
            for (int i = 0; i < data.size(); i++) {
                JsonNode embedding = data.get(i).get("embedding");
                float[] vec = new float[embedding.size()];
                for (int j = 0; j < embedding.size(); j++) {
                    vec[j] = (float) embedding.get(j).asDouble();
                }
                results.add(vec);
            }
        } catch (Exception e) {
            log.error("Batch embedding failed", e);
        }
        return results;
    }

    @Override
    public int dimension() {
        return 1536;
    }

    private String buildRequest(List<String> texts) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        if (texts.size() == 1) {
            root.put("input", texts.get(0));
        } else {
            ArrayNode arr = root.putArray("input");
            for (String t : texts) arr.add(t);
        }
        return MAPPER.writeValueAsString(root);
    }

    private String postJson(String urlStr, String json) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            try {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } finally { os.close(); }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                return readStream(conn.getInputStream());
            } else {
                String error = readStream(conn.getErrorStream());
                throw new RuntimeException("Embedding API error " + status + ": " + error);
            }
        } finally { conn.disconnect(); }
    }

    private static String readStream(InputStream input) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        return sb.toString();
    }
}
