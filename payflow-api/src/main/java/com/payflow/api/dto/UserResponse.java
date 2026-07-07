package com.payflow.api.dto;
import lombok.*;
@Getter @AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String name;
    private final String phoneNumber;
    private final String email;
}
