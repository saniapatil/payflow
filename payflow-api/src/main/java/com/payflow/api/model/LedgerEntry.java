package com.payflow.api.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "debit_account")
    private Long debitAccount;

    @Column(name = "credit_account")
    private Long creditAccount;

    private BigDecimal amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
