package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Services.SecurityService;
import com.daytrade.stocktrade.Services.StockService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

  private final StockService stockService;
  private final SecurityService securityService;

  @Autowired
  public TransactionController(StockService stockService, SecurityService securityService) {
    this.stockService = stockService;
    this.securityService = securityService;
  }

  @GetMapping("/quote/{stockSym}")
  public Map<String, Double> getQuote(@PathVariable("stockSym") String stockSym) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map<String, Double> out = new HashMap<>();
    Double quote = stockService.getQuote(name, stockSym);
    // Log here
    out.put(stockSym, quote);
    return out;
  }

  @PostMapping("/order")
  public Transaction createSimpleOrder(@Valid @RequestBody Transaction transaction) {
    if (transaction.getType().equals(Enums.TransactionType.SELL)
        || transaction.getType().equals(Enums.TransactionType.BUY)) {
      return transaction.getType().equals(Enums.TransactionType.BUY)
          ? stockService.createSimpleBuyTransaction(transaction)
          : stockService.createSimpleSellTransaction(transaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/order/limit")
  public Transaction createLimitOrder(@Valid @RequestBody Transaction transaction) {
    if (transaction.getType().equals(Enums.TransactionType.SELL_AT)
        || transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      return stockService.createLimitTransaction(transaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setBuy/trigger")
  public Transaction triggerBuyLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      Transaction savedTransaction = stockService.getPendingLimitBuyTransactions();
      return stockService.triggerLimitTransaction(savedTransaction, newTransaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/trigger")
  public Transaction triggerSellLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.SELL_AT)) {
      Transaction savedTransaction = stockService.getPendingLimitSellTransactions();
      return stockService.triggerLimitTransaction(savedTransaction, newTransaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/buy/cancel")
  public Transaction cancelBuyOrder() {
    Transaction transaction = stockService.getPendingBuyTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return stockService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/cancel")
  public Transaction cancelSellOrder() {
    Transaction transaction = stockService.getPendingSellTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return stockService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/commit")
  public Transaction commitSimpleSellOrder() {
    Transaction transaction = stockService.getPendingSellTransactions();
    transaction = stockService.commitSimpleOrder(transaction);
    stockService.updateAccount(transaction);
    return transaction;
  }

  @PostMapping("/buy/commit")
  public Account commitSimpleBuyOrder() {
    Transaction transaction = stockService.getPendingBuyTransactions();
    transaction = stockService.commitSimpleOrder(transaction);
    return stockService.updateAccount(transaction);
  }
}
