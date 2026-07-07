package com.payflow.api.dto;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentMessage {
    private Long transactionId;
    private Long senderId;
    private Long receiverId;
    private String idempotencyKey;
}
