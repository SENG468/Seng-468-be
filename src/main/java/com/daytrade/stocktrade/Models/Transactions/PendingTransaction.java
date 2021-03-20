package com.daytrade.stocktrade.Models.Transactions;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "pending_transactions")
@Data
public class PendingTransaction extends Transaction {
  @Id public String id;

  private Enums.TransactionStatus status = Enums.TransactionStatus.PENDING;

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
