package com.payflow.worker.dto;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {
    private Long transactionId;
    private Long senderId;
    private Long receiverId;
    private String idempotencyKey;
}
