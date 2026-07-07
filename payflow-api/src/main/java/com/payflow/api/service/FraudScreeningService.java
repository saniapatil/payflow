package com.payflow.api.service;
import com.payflow.api.dto.FraudResult;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
@Service
public class FraudScreeningService {
    private static final int BLOCK_THRESHOLD = 85;
    private final FraudDetectionService ruleBasedService;
    private final AIFraudService aiFraudService;
    public FraudScreeningService(FraudDetectionService ruleBasedService, AIFraudService aiFraudService) {
        this.ruleBasedService = ruleBasedService;
        this.aiFraudService = aiFraudService;
    }
    public FraudResult screen(Long senderId, BigDecimal amount) {
        FraudResult ruleResult = ruleBasedService.analyze(senderId, amount);
        int aiScore = aiFraudService.score(senderId, amount);
        int combinedScore = Math.max(ruleResult.getRiskScore(), aiScore);
        String reason = aiScore > ruleResult.getRiskScore() ? "Flagged by AI risk model" : ruleResult.getReason();
        return new FraudResult(combinedScore, reason, combinedScore >= BLOCK_THRESHOLD);
    }
}
