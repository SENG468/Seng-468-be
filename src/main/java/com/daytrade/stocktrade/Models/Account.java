package com.daytrade.stocktrade.Models;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
@Data
public class Account {

  public Account() {}

  public Account(String name) {
    this.name = name;
  }

  @Id private String id;

  private Double balance = 0D;

  @Indexed(unique = true)
  private String name;

  private Map<String, Long> portfolio = new HashMap<>();
}
