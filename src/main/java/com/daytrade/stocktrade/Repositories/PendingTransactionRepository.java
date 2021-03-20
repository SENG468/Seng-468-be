package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Models.Transactions.PendingTransaction;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingTransactionRepository extends MongoRepository<PendingTransaction, String> {

  List<Transaction> findByUserNameAndTypeOrderByCreatedDate(
      String userName, Enums.TransactionType buyAt);

  List<Transaction> findByUserNameAndTypeAndStockCodeOrderByCreatedDate(
      String userName, Enums.TransactionType sellAt, String stockTicker);

  List<Transaction> findAllByCreatedDateBefore(Instant minus);
}
