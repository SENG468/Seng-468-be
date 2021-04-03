package com.daytrade.stocktrade.Models;

import com.daytrade.stocktrade.Models.Transactions.PendingTransaction;
import com.daytrade.stocktrade.Models.Transactions.Transaction;
import java.util.List;
import lombok.Data;

@Data
public class Summary {

  public Summary(String username, Account account) {
    this.username = username;
    this.account = account;
  }

  public String username;

  public Account account;

  public List<Transaction> closedTransactions;

  public List<Transaction> openTriggers;

  public List<PendingTransaction> pendingTransactions;
}
