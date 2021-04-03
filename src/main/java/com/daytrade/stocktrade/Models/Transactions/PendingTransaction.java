package com.daytrade.stocktrade.Models.Transactions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "pending_transactions")
@Data
public class PendingTransaction extends Transaction {}
