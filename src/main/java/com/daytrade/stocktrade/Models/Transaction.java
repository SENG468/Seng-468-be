package com.daytrade.stocktrade.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transactions")
@Data
public class Transaction {
  @Id public String id;

  @Indexed
  private Enums.TransactionStatus status;

  @NotNull @Indexed private Enums.TransactionType type;

  @NotNull @NotBlank private String stockCode;

  private Long stockAmount;

  private Double cashAmount;

  @Indexed private String userName;

  @CreatedDate @Indexed private Instant createdDate;

  private String transactionId;

  private Double unitPrice;

  public void setCashAmount(Double cashAmount) {
    this.cashAmount = BigDecimal.valueOf(cashAmount).setScale(2, RoundingMode.FLOOR).doubleValue();
  }
}
