package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Command;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Services.LoggerService;
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

  @Autowired
  public TransactionController(TransactionService transactionService, LoggerService loggerService) {
    this.transactionService = transactionService;
    this.loggerService = loggerService;
  }

  @GetMapping("/quote/{stockSym}")
  public Map<String, Double> getQuote(
      @PathVariable("stockSym") String stockSym, @RequestParam(name = "transactionId") String transId) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map<String, Double> out = new HashMap<>();
    loggerService.createCommandLog(name, transId, Enums.CommandType.QUOTE, stockSym, null, null);
    Double quote =
        transactionService.getQuote(name, stockSym, transId).getUnitPrice();
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
      transaction.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
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
          Enums.CommandType cmdType =
          transaction.getType().equals(Enums.TransactionType.SELL_AT)
              ? Enums.CommandType.SET_SELL_AMOUNT
              : Enums.CommandType.SET_BUY_AMOUNT;
      transaction.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
      loggerService.createTransactionCommandLog(transaction, cmdType, null);
      Transaction newTransaction = transactionService.createLimitTransaction(transaction);
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
      String name = SecurityContextHolder.getContext().getAuthentication().getName();
      newTransaction.setUserName(name);
      loggerService.createTransactionCommandLog(
        newTransaction, Enums.CommandType.SET_BUY_TRIGGER, null);
      Command cmd = new Command();
      cmd.setTransactionId(newTransaction.getTransactionId());
      cmd.setUsername(name);
      cmd.setType(Enums.CommandType.SET_BUY_TRIGGER);
      Transaction savedTransaction = transactionService.getPendingLimitBuyTransactions(cmd);
      Transaction updatedTransaction =
          transactionService.triggerLimitTransaction(savedTransaction, newTransaction);
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
      String name = SecurityContextHolder.getContext().getAuthentication().getName();
      newTransaction.setUserName(name);
      loggerService.createTransactionCommandLog(
        newTransaction, Enums.CommandType.SET_SELL_TRIGGER, null);
      Command cmd = new Command();
      cmd.setTransactionId(newTransaction.getTransactionId());
      cmd.setUsername(name);
      cmd.setType(Enums.CommandType.SET_SELL_TRIGGER);
      Transaction savedTransaction = transactionService.getPendingLimitSellTransactions(cmd);
      Transaction updatedTransaction =
          transactionService.triggerLimitTransaction(savedTransaction, newTransaction);

      return updatedTransaction;
    } else {
      loggerService.createTransactionErrorLog(
          newTransaction, Enums.CommandType.SET_SELL_TRIGGER, "Incorrect transaction type");
      throw new BadRequestException("Not correct transaction type");
    }
  }

  @PostMapping("/setSell/cancel/{stock}")
  public Transaction cancelSellLimitOrder(
      @Valid @RequestBody Command cmd, @PathVariable("stock") String stockTicker) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.CANCEL_SET_SELL);
    try {
      loggerService.createCommandLog(
        name, cmd.getTransactionId(), Enums.CommandType.CANCEL_SET_SELL, stockTicker, null, null);
      Transaction savedTransaction =
          transactionService.getPendingLimitSellTransactionsByTicker(stockTicker);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      savedTransaction.setTransactionId(cmd.getTransactionId());
      return transactionService.cancelSellLimitTransaction(savedTransaction);
    } catch (EntityMissingException ex) {
      Transaction savedTransaction =
          transactionService.getCommittedLimitSellTransactionsByTicker(stockTicker, cmd);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      return transactionService.cancelSellLimitTransaction(savedTransaction);
    }
  }

  @PostMapping("/setBuy/cancel/{stock}")
  public Transaction cancelBuyLimitOrder(
      @Valid @RequestBody Command cmd, @PathVariable("stock") String stockTicker) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.CANCEL_SET_BUY);
    try {
      loggerService.createCommandLog(
        name, cmd.getTransactionId(), Enums.CommandType.CANCEL_SET_BUY, stockTicker, null, null);
      Transaction savedTransaction =
          transactionService.getPendingLimitBuyTransactionsByTicker(stockTicker);
      savedTransaction.setStatus(Enums.TransactionStatus.CANCELED);
      savedTransaction.setTransactionId(cmd.getTransactionId());
      return transactionService.cancelBuyLimitTransaction(savedTransaction);
    } catch (EntityMissingException ex) {
      Transaction savedTransaction =
          transactionService.getCommittedLimitBuyTransactionsByTicker(stockTicker, cmd);
      return transactionService.cancelBuyLimitTransaction(savedTransaction);
    }
  }

  @PostMapping("/buy/cancel")
  public Transaction cancelBuyOrder(@Valid @RequestBody Command cmd) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.CANCEL_BUY);
    loggerService.createCommandLog(
      name, cmd.getTransactionId(), Enums.CommandType.CANCEL_BUY, null, null, null);
    Transaction transaction = transactionService.getPendingBuyTransactions(cmd);
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/cancel")
  public Transaction cancelSellOrder(@Valid @RequestBody Command cmd) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.CANCEL_SELL);
    loggerService.createCommandLog(
      name, cmd.getTransactionId(), Enums.CommandType.CANCEL_SELL, null, null, null);
    Transaction transaction = transactionService.getPendingSellTransactions(cmd);
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionService.cancelTransaction(transaction);
  }

  @PostMapping("/sell/commit")
  public Transaction commitSimpleSellOrder(@Valid @RequestBody Command cmd) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.COMMIT_SELL);
    loggerService.createCommandLog(
      name, cmd.getTransactionId(), Enums.CommandType.COMMIT_SELL, null, null, null);
    Transaction transaction = transactionService.getPendingSellTransactions(cmd);
    transaction = transactionService.commitSimpleOrder(transaction);
    transactionService.updateAccount(transaction);
    return transaction;
  }

  @PostMapping("/buy/commit")
  public Account commitSimpleBuyOrder(@Valid @RequestBody Command cmd) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    cmd.setUsername(name);
    cmd.setType(Enums.CommandType.COMMIT_BUY);
    loggerService.createCommandLog(
      name, cmd.getTransactionId(), Enums.CommandType.COMMIT_BUY, null, null, null);
    Transaction transaction = transactionService.getPendingBuyTransactions(cmd);
    transaction = transactionService.commitSimpleOrder(transaction);
    return transactionService.updateAccount(transaction);
  }
}
