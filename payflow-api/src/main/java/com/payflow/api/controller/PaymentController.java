package com.payflow.api.controller;
import com.payflow.api.dto.*;
import com.payflow.api.model.Transaction;
import com.payflow.api.model.User;
import com.payflow.api.security.AuthUtil;
import com.payflow.api.security.AuthenticatedUser;
import com.payflow.api.service.PaymentService;
import com.payflow.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    private final AuthUtil authUtil;

    public PaymentController(PaymentService paymentService, UserService userService, AuthUtil authUtil) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.authUtil = authUtil;
    }
    @PostMapping("/transfer-by-phone")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request) {
        AuthenticatedUser caller = authUtil.getCurrentUser();
        User sender = userService.findById(caller.userId());
        if (sender == null || !userService.checkPassword(request.getPassword(), sender.getPassword())) {
            return ResponseEntity.status(401).body("Incorrect password");
        }
        try {
            String result = paymentService.transfer(
                    caller.userId(),
                    request.getReceiverPhone(),
                    request.getAmount(),
                    request.getIdempotencyKey()
            );
            return ResponseEntity.ok(result);
        } catch (PaymentService.TransferException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/add-money")
    public ResponseEntity<?> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        AuthenticatedUser caller = authUtil.getCurrentUser();
        try {
            String result = paymentService.addMoney(caller.userId(), request.getAmount());
            return ResponseEntity.ok(result);
        } catch (PaymentService.TransferException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        AuthenticatedUser caller = authUtil.getCurrentUser();
        return ResponseEntity.ok(paymentService.getBalance(caller.userId()));
    }
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions() {
        AuthenticatedUser caller = authUtil.getCurrentUser();
        return ResponseEntity.ok(paymentService.getTransactions(caller.userId()));
    }
}
