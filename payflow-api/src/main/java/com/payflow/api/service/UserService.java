package com.payflow.api.service;
import com.payflow.api.model.Account;
import com.payflow.api.model.User;
import com.payflow.api.repository.AccountRepository;
import com.payflow.api.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Service
public class UserService {
    private static final BigDecimal SIGNUP_BONUS = new BigDecimal("5000.00");
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public User createUser(String name, String phoneNumber, String email, String rawPassword) {
        if (userRepository.findByPhoneNumber(phoneNumber) != null) {
            throw new IllegalStateException("Phone number already registered");
        }
        User user = new User();
        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        Account account = new Account();
        account.setUser(saved);
        account.setBalance(SIGNUP_BONUS);
        accountRepository.save(account);
        return saved;
    }
    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
