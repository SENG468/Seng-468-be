package com.daytrade.stocktrade.Models.Transactions;

import com.daytrade.stocktrade.Models.Transaction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "pending_transactions")
@Data
public class PendingTransaction extends Transaction {}
