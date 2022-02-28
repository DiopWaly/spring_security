package sn.sun.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.sun.batch.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction,Long> {
}
