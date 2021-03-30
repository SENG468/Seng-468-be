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

  List<Transaction> findByUserNameAndTypeAndStatusAndStockCodeOrderByCreatedDate(
      String userName,
      Enums.TransactionType sellAt,
      Enums.TransactionStatus pending,
      String stockCode);

  List<Transaction> findAllByStatusAndType(
      Enums.TransactionStatus status, Enums.TransactionType type);

  List<Transaction> findByUserNameAndStatusInOrderByCreatedDate(
      String name, List<Enums.TransactionStatus> status);

  List<Transaction> findByUserNameAndStatusNotInOrderByCreatedDate(String username, List<Enums.TransactionStatus> status);
}
