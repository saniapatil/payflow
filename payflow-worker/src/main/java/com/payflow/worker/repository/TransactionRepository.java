package com.payflow.worker.repository;
import com.payflow.worker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
