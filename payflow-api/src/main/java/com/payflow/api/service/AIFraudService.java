package com.payflow.api.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
@Service
public class AIFraudService {
    @Value("${groq.api.key}")
    private String apiKey;
    private final WebClient webClient = WebClient.create("https://api.groq.com/openai/v1/chat/completions");
    private final ObjectMapper mapper = new ObjectMapper();
    public int score(Long senderId, BigDecimal amount) {
        String prompt = "You are a fraud detection system for a payments app. "
                + "Sender ID: " + senderId + ", Amount: Rs " + amount + ". "
                + "Reply with ONLY JSON, no markdown: {\"riskScore\": <0-100>, \"reason\": \"<short reason>\"}";
        Map<String, Object> body = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        try {
            String raw = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(4))
                    .onErrorResume(e -> Mono.empty())
                    .block();
            if (raw == null) return 0;
            String content = mapper.readTree(raw)
                    .path("choices").get(0).path("message").path("content").asText();
            content = content.replace("```json", "").replace("```", "").trim();
            return mapper.readTree(content).path("riskScore").asInt(0);
        } catch (Exception e) {
            return 0;
        }
    }
}
