package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Transaction;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

  List<Transaction> findByUserNameAndTypeAndStatusOrderByCreatedDate(
      String s, Enums.TransactionType type, Enums.TransactionStatus status);
}
