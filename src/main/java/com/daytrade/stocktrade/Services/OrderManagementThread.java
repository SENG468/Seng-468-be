package com.daytrade.stocktrade.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class OrderManagementThread {

  private final TransactionService transactionService;

  @Autowired
  public OrderManagementThread(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  // Expire orders created more than a minute ago
  @Scheduled(fixedDelay = 1000)
  public void expireOrders() {
    transactionService.expireOrders();
  }

  // Check if any of the limit sell orders can be filled
  @Scheduled(fixedDelay = 60000)
  public void fillSellLimitOrders() throws InterruptedException {
    transactionService.fillSellLimitOrders();
  }

  // Check if any of the limit buy orders can be filled
  @Scheduled(fixedDelay = 60000)
  public void fillBuyLimitOrders() throws InterruptedException {
    transactionService.fillBuyLimitOrders();
  }
}
