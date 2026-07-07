package com.payflow.worker.repository;
import com.payflow.worker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.user.id = :userId")
    Account findByUserIdForUpdate(Long userId);
}
