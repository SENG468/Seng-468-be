package com.daytrade.stocktrade.Models;

import lombok.Data;

@Data
public class Command {
  public String transactionId;
  public String username;
  public Enums.CommandType type;
}
