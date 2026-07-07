package com.payflow.api.controller;
import com.payflow.api.dto.*;
import com.payflow.api.model.User;
import com.payflow.api.security.JwtUtil;
import com.payflow.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user;
        try {
            user = userService.createUser(request.getName(), request.getPhoneNumber(), request.getEmail(), request.getPassword());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        String token = jwtUtil.generateToken(user.getId(), user.getPhoneNumber());
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getPhoneNumber(), user.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByPhoneNumber(request.getPhoneNumber());
        if (user == null || !userService.checkPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid phone number or password");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getPhoneNumber());
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getPhoneNumber(), user.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }
}
