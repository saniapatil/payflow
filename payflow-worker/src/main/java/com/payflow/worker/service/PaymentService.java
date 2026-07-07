package com.payflow.worker.service;
import com.payflow.worker.dto.PaymentMessage;
import com.payflow.worker.model.*;
import com.payflow.worker.repository.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service
public class PaymentService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final RedisLockService redisLockService;
    private final StringRedisTemplate redisTemplate;
    private final TransactionTemplate transactionTemplate;
    public PaymentService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository,
            RedisLockService redisLockService,
            StringRedisTemplate redisTemplate,
            PlatformTransactionManager transactionManager
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.redisLockService = redisLockService;
        this.redisTemplate = redisTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }
    static class RetryableProcessingException extends RuntimeException {
        RetryableProcessingException(String message) { super(message); }
        RetryableProcessingException(String message, Throwable cause) { super(message, cause); }
    }
    public void processPayment(PaymentMessage message) {

        Transaction transaction = transactionRepository.findById(message.getTransactionId()).orElse(null);
        if (transaction == null) {
            
            System.err.println("No transaction found for id " + message.getTransactionId() + ", dropping message");
            return;
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            
            System.out.println("Transaction " + transaction.getId() + " already " + transaction.getStatus() + ", skipping");
            return;
        }
        Long senderId = message.getSenderId();
        Long receiverId = message.getReceiverId();
        if (senderId == null) {
            processDeposit(transaction, receiverId);
            return;
        }
        Long firstLockUser = Math.min(senderId, receiverId);
        Long secondLockUser = Math.max(senderId, receiverId);
        String firstLockKey = "lock:user:" + firstLockUser;
        String secondLockKey = "lock:user:" + secondLockUser;
        String firstToken = redisLockService.lock(firstLockKey);
        if (firstToken == null) {
            throw new RetryableProcessingException("Could not acquire lock for user " + firstLockUser);
        }
        try {
            String secondToken = redisLockService.lock(secondLockKey);
            if (secondToken == null) {
                throw new RetryableProcessingException("Could not acquire lock for user " + secondLockUser);
            }

            try {
                doTransferInTransaction(transaction, senderId, receiverId);
            } finally {
                redisLockService.unlock(secondLockKey, secondToken);
            }

        } finally {
            redisLockService.unlock(firstLockKey, firstToken);
        }
    }
    private void processDeposit(Transaction transaction, Long receiverId) {
        String lockKey = "lock:user:" + receiverId;
        String token = redisLockService.lock(lockKey);
        if (token == null) {
            throw new RetryableProcessingException("Could not acquire lock for user " + receiverId);
        }
        try {
            transactionTemplate.executeWithoutResult(status -> {
                Account receiver = accountRepository.findByUserIdForUpdate(receiverId);

                if (receiver == null) {
                    failTransaction(transaction, "Account Not Found");
                    return;
                }

                BigDecimal amount = transaction.getAmount();
                receiver.setBalance(receiver.getBalance().add(amount));
                accountRepository.save(receiver);

                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);

                LedgerEntry entry = new LedgerEntry();
                entry.setTransactionId(transaction.getId());
                entry.setDebitAccount(null);
                entry.setCreditAccount(receiver.getId());
                entry.setAmount(amount);
                entry.setCreatedAt(LocalDateTime.now());
                ledgerRepository.save(entry);

                redisTemplate.delete("balance:" + receiverId);
            });
        } finally {
            redisLockService.unlock(lockKey, token);
        }
    }

    private void doTransferInTransaction(Transaction transaction, Long senderId, Long receiverId) {
        transactionTemplate.executeWithoutResult(status -> {

            Long firstUser = Math.min(senderId, receiverId);
            Long secondUser = Math.max(senderId, receiverId);
            Account firstLocked = accountRepository.findByUserIdForUpdate(firstUser);
            Account secondLocked = accountRepository.findByUserIdForUpdate(secondUser);

            Account sender = senderId.equals(firstUser) ? firstLocked : secondLocked;
            Account receiver = receiverId.equals(firstUser) ? firstLocked : secondLocked;

            if (sender == null || receiver == null) {
                failTransaction(transaction, "Account Not Found");
                return;
            }

            BigDecimal amount = transaction.getAmount();

            if (sender.getBalance().compareTo(amount) < 0) {
                failTransaction(transaction, "Insufficient Balance");
                return;
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));

            accountRepository.save(sender);
            accountRepository.save(receiver);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            LedgerEntry entry = new LedgerEntry();
            entry.setTransactionId(transaction.getId());
            entry.setDebitAccount(sender.getId());
            entry.setCreditAccount(receiver.getId());
            entry.setAmount(amount);
            entry.setCreatedAt(LocalDateTime.now());
            ledgerRepository.save(entry);

            redisTemplate.delete("balance:" + senderId);
            redisTemplate.delete("balance:" + receiverId);
        });
    }

    private void failTransaction(Transaction transaction, String reason) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(reason);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}
