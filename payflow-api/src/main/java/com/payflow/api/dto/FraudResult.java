package com.payflow.api.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FraudResult {
    private int riskScore;
    private String reason;
    private boolean blocked;
}
