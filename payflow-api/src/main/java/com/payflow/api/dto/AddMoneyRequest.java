package com.payflow.api.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddMoneyRequest {
    @NotNull
    @DecimalMin(value = "1.0", message = "Minimum amount is 1")
    private BigDecimal amount;
}
