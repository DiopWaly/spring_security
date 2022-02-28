package sn.sun.batch;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sn.sun.dao.BankTransactionRepository;

import java.util.List;

@Component
public class BankTransactionItemWriter implements ItemWriter<BankTransaction> {
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Override
    public void write(List<? extends BankTransaction> bankTransactions) throws Exception {
        bankTransactionRepository.saveAll(bankTransactions);
    }
}
