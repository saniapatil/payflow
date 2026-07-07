package com.payflow.worker.repository;
import com.payflow.worker.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
}
