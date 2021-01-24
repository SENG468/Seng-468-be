package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Services.SecurityService;
import com.daytrade.stocktrade.Services.TransactionService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

  private final TransactionService transactionService;
  private final SecurityService securityService;

  @Autowired
  public TransactionController(
      TransactionService transactionService, SecurityService securityService) {
    this.transactionService = transactionService;
    this.securityService = securityService;
  }

  @GetMapping("/quote/{stockSym}")
  public Map<String, Double> getQuote(@PathVariable("stockSym") String stockSym) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map<String, Double> out = new HashMap<>();
    Double quote = transactionService.getQuote(name, stockSym);
    // Log here
    out.put(stockSym, quote);
    return out;
  }

  @PostMapping("/order/simple")
  public Transaction createSimpleOrder(@Valid @RequestBody Transaction transaction) {
    if (transaction.getType().equals(Enums.TransactionType.SELL)
        || transaction.getType().equals(Enums.TransactionType.BUY)) {
      return transaction.getType().equals(Enums.TransactionType.BUY)
          ? transactionService.createSimpleBuyTransaction(transaction)
          : transactionService.createSimpleSellTransaction(transaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/order/limit")
  public Transaction createLimitOrder(@Valid @RequestBody Transaction transaction) {
    if (transaction.getType().equals(Enums.TransactionType.SELL_AT)
        || transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      return transactionService.createLimitTransaction(transaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setBuy/trigger")
  public Transaction triggerBuyLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      Transaction savedTransaction = transactionService.getPendingLimitBuyTransactions();
      return transactionService.triggerLimitTransaction(savedTransaction, newTransaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/trigger")
  public Transaction triggerSellLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.SELL_AT)) {
      Transaction savedTransaction = transactionService.getPendingLimitSellTransactions();
      return transactionService.triggerLimitTransaction(savedTransaction, newTransaction);
    } else {
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/cancel")
  public Transaction cancelSellLimitOrder() {
    Transaction savedTransaction = transactionService.getPendingLimitSellTransactions();
    savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelSellLimitTransaction(savedTransaction);
  }

  @PostMapping("/setBuy/cancel")
  public Transaction cancelBuyLimitOrder() {
    Transaction savedTransaction = transactionService.getPendingLimitBuyTransactions();
    savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelBuyLimitTransaction(savedTransaction);
  }

  @PostMapping("/buy/cancel")
  public Transaction cancelBuyOrder() {
    Transaction transaction = transactionService.getPendingBuyTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/cancel")
  public Transaction cancelSellOrder() {
    Transaction transaction = transactionService.getPendingSellTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/commit")
  public Transaction commitSimpleSellOrder() {
    Transaction transaction = transactionService.getPendingSellTransactions();
    transaction = transactionService.commitSimpleOrder(transaction);
    transactionService.updateAccount(transaction);
    return transaction;
  }

  @PostMapping("/buy/commit")
  public Account commitSimpleBuyOrder() {
    Transaction transaction = transactionService.getPendingBuyTransactions();
    transaction = transactionService.commitSimpleOrder(transaction);
    return transactionService.updateAccount(transaction);
  }
}
