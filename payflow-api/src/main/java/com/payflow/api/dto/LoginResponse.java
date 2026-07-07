package com.payflow.api.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class LoginResponse {
    private final String token;
    private final UserResponse user;
}
