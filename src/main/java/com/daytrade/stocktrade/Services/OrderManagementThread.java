package com.daytrade.stocktrade.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OrderManagementThread {

  private final AccountService accountService;

  private final TransactionService transactionService;

  @Autowired
  public OrderManagementThread(
      AccountService accountService, TransactionService transactionService) {
    this.accountService = accountService;
    this.transactionService = transactionService;
  }

  // Expire orders created more than a minute ago
  @Scheduled(fixedDelay = 60000)
  public void expireOrders() {}

  // Check if any of the limit sell orders can be filled
  @Scheduled(fixedDelay = 60000)
  public void fillSellLimitOrders() {}

  // Check if any of the limit buy orders can be filled
  @Scheduled(fixedDelay = 60000)
  public void fillBuyLimitOrders() {}
}
