package com.payflow.api.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TransferRequest {
    @NotBlank
    private String receiverPhone;
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    @NotBlank
    private String idempotencyKey;
    @NotBlank
    private String password;
}
