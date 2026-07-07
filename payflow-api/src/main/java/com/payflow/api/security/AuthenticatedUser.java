package com.payflow.api.security;

public record AuthenticatedUser(Long userId, String phoneNumber) {
}
