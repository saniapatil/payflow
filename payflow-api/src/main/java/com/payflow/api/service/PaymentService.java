package com.payflow.api.service;
import com.payflow.api.dto.FraudResult;
import com.payflow.api.dto.PaymentMessage;
import com.payflow.api.model.*;
import com.payflow.api.producer.PaymentProducer;
import com.payflow.api.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
public class PaymentService {
    private static final BigDecimal MAX_ADD_MONEY = new BigDecimal("10000");
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentProducer paymentProducer;
    private final FraudScreeningService fraudScreeningService;
    private final StringRedisTemplate redisTemplate;
    public PaymentService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            PaymentProducer paymentProducer,
            FraudScreeningService fraudScreeningService,
            StringRedisTemplate redisTemplate
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.paymentProducer = paymentProducer;
        this.fraudScreeningService = fraudScreeningService;
        this.redisTemplate = redisTemplate;
    }
    public static class TransferException extends RuntimeException {
        public TransferException(String message) { super(message); }
    }
    @Transactional
    public String transfer(Long senderId, String receiverPhone, BigDecimal amount, String idempotencyKey) {
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return describeStatus(existing.get());
        }
        User receiverUser = userRepository.findByPhoneNumber(receiverPhone);
        if (receiverUser == null) {
            throw new TransferException("No PayFlow user found with that phone number");
        }
        if (receiverUser.getId().equals(senderId)) {
            throw new TransferException("You can't send money to yourself");
        }
        Account senderAccount = accountRepository.findByUserId(senderId);
        if (senderAccount == null) {
            throw new TransferException("Sender account not found");
        }
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Insufficient balance");
        }
        FraudResult fraud = fraudScreeningService.screen(senderId, amount); 
        User senderUser = userRepository.findById(senderId).orElse(null);
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverUser.getId());
        transaction.setAmount(amount);
        transaction.setSenderName(senderUser != null ? senderUser.getName() : "Unknown");
        transaction.setReceiverName(receiverUser.getName());
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        if (fraud.isBlocked()) {
            transaction.setStatus(TransactionStatus.BLOCKED);
            transaction.setFailureReason(fraud.getReason());
            saveTransaction(transaction);
            throw new TransferException("Transaction blocked: " + fraud.getReason());
        }
        transaction.setStatus(TransactionStatus.PENDING);
        Transaction saved = saveTransaction(transaction);
        paymentProducer.sendPayment(new PaymentMessage(saved.getId(), senderId, receiverUser.getId(), idempotencyKey));
        return "Payment Processing...";
    }
    public String addMoney(Long userId, BigDecimal amount) {
        if (amount.compareTo(MAX_ADD_MONEY) > 0) {
            throw new TransferException("You can add a maximum of Rs " + MAX_ADD_MONEY + " at a time");
        }
        Account account = accountRepository.findByUserId(userId);
        if (account == null) {
            throw new TransferException("Account not found");
        }
        User user = userRepository.findById(userId).orElse(null);
        String idempotencyKey = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setSenderId(null);
        transaction.setReceiverId(userId);
        transaction.setAmount(amount);
        transaction.setSenderName("PayFlow");
        transaction.setReceiverName(user != null ? user.getName() : "Unknown");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction saved = saveTransaction(transaction);
        paymentProducer.sendPayment(new PaymentMessage(saved.getId(), null, userId, idempotencyKey));
        return "Money Added Successfully";
    }
    private Transaction saveTransaction(Transaction transaction) {
        try {
            return transactionRepository.save(transaction);
        } catch (DataIntegrityViolationException e) {
            
            throw new TransferException("Duplicate request detected");
        }
    }
    private String describeStatus(Transaction t) {
        return switch (t.getStatus()) {
            case PENDING -> "Payment Processing...";
            case SUCCESS -> "Transfer Successful";
            case FAILED -> "Transfer failed: " + t.getFailureReason();
            case BLOCKED -> "Transaction blocked: " + t.getFailureReason();
        };
    }
    public BigDecimal getBalance(Long userId) {
        String cacheKey = "balance:" + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return new BigDecimal(cached);
        }
        Account account = accountRepository.findByUserId(userId);
        BigDecimal balance = account != null ? account.getBalance() : BigDecimal.ZERO;
        redisTemplate.opsForValue().set(cacheKey, balance.toPlainString());
        return balance;
    }
    public List<Transaction> getTransactions(Long userId) {
        return transactionRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);
    }
}
