package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Services.LoggerService;
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
  private final LoggerService loggerService;
  private final SecurityService securityService;

  @Autowired
  public TransactionController(
      TransactionService transactionService,
      SecurityService securityService,
      LoggerService loggerService) {
    this.transactionService = transactionService;
    this.loggerService = loggerService;
    this.securityService = securityService;
  }

  @GetMapping("/quote/{stockSym}")
  public Map<String, Double> getQuote(@PathVariable("stockSym") String stockSym) throws Exception {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map<String, Double> out = new HashMap<>();
    Double quote = transactionService.getQuote(name, stockSym, "9999"); // Need cmd id here
    loggerService.createCommandLog(name, "temp", Enums.CommandType.QUOTE, stockSym, null, quote);
    out.put(stockSym, quote);
    return out;
  }

  @PostMapping("/order/simple")
  public Transaction createSimpleOrder(@Valid @RequestBody Transaction transaction)
      throws Exception {
    if (transaction.getType().equals(Enums.TransactionType.SELL)
        || transaction.getType().equals(Enums.TransactionType.BUY)) {
      Enums.CommandType cmdType =
          transaction.getType().equals(Enums.TransactionType.SELL)
              ? Enums.CommandType.SELL
              : Enums.CommandType.BUY;
      loggerService.createTransactionCommandLog(transaction, cmdType, null);
      return transaction.getType().equals(Enums.TransactionType.BUY)
          ? transactionService.createSimpleBuyTransaction(transaction)
          : transactionService.createSimpleSellTransaction(transaction);
    } else {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.BUY, "Incorrect transaction type");
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/order/limit")
  public Transaction createLimitOrder(@Valid @RequestBody Transaction transaction) {
    if (transaction.getType().equals(Enums.TransactionType.SELL_AT)
        || transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      Transaction newTransaction = transactionService.createLimitTransaction(transaction);
      Enums.CommandType cmdType =
          transaction.getType().equals(Enums.TransactionType.SELL_AT)
              ? Enums.CommandType.SET_SELL_AMOUNT
              : Enums.CommandType.SET_BUY_AMOUNT;
      loggerService.createTransactionCommandLog(newTransaction, cmdType, null);
      return newTransaction;
    } else {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.SET_BUY_AMOUNT, "Incorrect transaction type");
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setBuy/trigger")
  public Transaction triggerBuyLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      Transaction savedTransaction = transactionService.getPendingLimitBuyTransactions();
      Transaction updatedTransaction =
          transactionService.triggerLimitTransaction(savedTransaction, newTransaction);
      loggerService.createTransactionCommandLog(
          updatedTransaction, Enums.CommandType.SET_BUY_TRIGGER, null);
      return updatedTransaction;
    } else {
      loggerService.createTransactionErrorLog(
          newTransaction, Enums.CommandType.SET_BUY_TRIGGER, "Incorrect transaction type");
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/trigger")
  public Transaction triggerSellLimitOrder(@Valid @RequestBody Transaction newTransaction) {
    if (newTransaction.getType().equals(Enums.TransactionType.SELL_AT)) {
      Transaction savedTransaction = transactionService.getPendingLimitSellTransactions();
      Transaction updatedTransaction =
          transactionService.triggerLimitTransaction(savedTransaction, newTransaction);
      loggerService.createTransactionCommandLog(
          updatedTransaction, Enums.CommandType.SET_SELL_TRIGGER, null);
      return updatedTransaction;
    } else {
      loggerService.createTransactionErrorLog(
          newTransaction, Enums.CommandType.SET_SELL_TRIGGER, "Incorrect transaction type");
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/cancel/{stock}")
  public Transaction cancelSellLimitOrder(@PathVariable("stock") String stockTicker) {
    try {
      Transaction savedTransaction =
          transactionService.getPendingLimitSellTransactionsByTicker(stockTicker);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      loggerService.createTransactionCommandLog(
          savedTransaction, Enums.CommandType.CANCEL_SET_SELL, stockTicker);
      return transactionService.cancelSellLimitTransaction(savedTransaction);
    } catch (EntityMissingException ex) {
      Transaction savedTransaction =
          transactionService.getCommittedLimitSellTransactionsByTicker(stockTicker);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      loggerService.createTransactionErrorLog(
          savedTransaction, Enums.CommandType.CANCEL_SET_SELL, "Missing Entity");
      return transactionService.cancelSellLimitTransaction(savedTransaction);
    }
  }

  @PostMapping("/setBuy/cancel/{stock}")
  public Transaction cancelBuyLimitOrder(@PathVariable("stock") String stockTicker) {
    try {
      Transaction savedTransaction =
          transactionService.getPendingLimitBuyTransactionsByTicker(stockTicker);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      loggerService.createTransactionCommandLog(
          savedTransaction, Enums.CommandType.CANCEL_SET_BUY, stockTicker);
      return transactionService.cancelBuyLimitTransaction(savedTransaction);
    } catch (EntityMissingException ex) {
      Transaction savedTransaction =
          transactionService.getCommittedLimitBuyTransactionsByTicker(stockTicker);
      loggerService.createTransactionErrorLog(
          savedTransaction, Enums.CommandType.CANCEL_SET_BUY, "Missing Entity");
      return transactionService.cancelBuyLimitTransaction(savedTransaction);
    }
  }

  @PostMapping("/buy/cancel")
  public Transaction cancelBuyOrder() {
    Transaction transaction = transactionService.getPendingBuyTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    loggerService.createTransactionCommandLog(transaction, Enums.CommandType.CANCEL_BUY, null);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/cancel")
  public Transaction cancelSellOrder() {
    Transaction transaction = transactionService.getPendingSellTransactions();
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    loggerService.createTransactionCommandLog(transaction, Enums.CommandType.CANCEL_SELL, null);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/commit")
  public Transaction commitSimpleSellOrder() {
    Transaction transaction = transactionService.getPendingSellTransactions();
    transaction = transactionService.commitSimpleOrder(transaction);
    transactionService.updateAccount(transaction);
    loggerService.createTransactionCommandLog(transaction, Enums.CommandType.COMMIT_SELL, null);
    return transaction;
  }

  @PostMapping("/buy/commit")
  public Account commitSimpleBuyOrder() {
    Transaction transaction = transactionService.getPendingBuyTransactions();
    transaction = transactionService.commitSimpleOrder(transaction);
    loggerService.createTransactionCommandLog(transaction, Enums.CommandType.COMMIT_BUY, null);
    return transactionService.updateAccount(transaction);
  }
}
