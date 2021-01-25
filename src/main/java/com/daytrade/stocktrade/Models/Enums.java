package com.daytrade.stocktrade.Models;

public class Enums {

  public enum TransactionStatus {
    PENDING,
    CANCELED,
    COMMITTED,
    FILLED,
    EXPIRED
  }

  public enum TransactionType {
    BUY,
    SELL,
    BUY_AT,
    SELL_AT
  }

  public enum CommandType {
    ADD,
    QUOTE,
    BUY,
    COMMIT_BUY,
    CANCEL_BUY,
    SELL,
    COMMIT_SELL,
    CANCEL_SELL,
    SET_BUY_AMOUNT,
    CANCEL_SET_BUY,
    SET_BUY_TRIGGER,
    SET_SELL_AMOUNT,
    SET_SELL_TRIGGER,
    CANCEL_SET_SELL,
    DUMPLOG,
    DISPLAY_SUMMARY
  }

  public enum LogType {
    UserCommandType,
    QuoteServerType,
    AccountTransactionType,
    SystemEventType,
    ErrorEventType,
    DebugType
  }
}
