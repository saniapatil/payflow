package com.payflow.worker.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
