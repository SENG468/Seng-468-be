package com.daytrade.stocktrade.Models;

import java.time.Instant;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "logs")
@Data
public class Logger {

  public Logger() {}

  public Logger(Enums.LogType logType, String transactionNumber, String serverName) {
    this.logType = logType;
    this.serverName = serverName;
    this.transactionNumber = transactionNumber;
  }

  @Id public String id;

  @NotNull private Enums.LogType logType;

  @NotBlank private String serverName;

  @NotNull private String transactionNumber;

  private Enums.CommandType commandType;

  @Indexed String userName;

  @CreatedDate @Indexed private Instant timestamp;

  private String stockSymbol;

  private String fileName;

  private Double funds;

  private Double unitPrice;

  private Instant quoteServerTime;

  private String cryptoKey;

  private String action;

  private String message;
}
