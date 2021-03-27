package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Transactions.PendingTransaction;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingTransactionRepository extends MongoRepository<PendingTransaction, String> {

  List<PendingTransaction> findByUserNameAndTypeOrderByCreatedDate(
      String userName, Enums.TransactionType buyAt);

  List<PendingTransaction> findByUserNameAndTypeAndStockCodeOrderByCreatedDate(
      String userName, Enums.TransactionType sellAt, String stockTicker);

  List<PendingTransaction> findAllByCreatedDateBefore(Instant minus);

  List<PendingTransaction> findByUserNameOrderByCreatedDate(String username);
}
