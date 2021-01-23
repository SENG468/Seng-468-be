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
}
