package com.daytrade.stocktrade.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Logger;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Repositories.LoggerRepository;

@Service
public class LoggerService {
  private final LoggerRepository loggerRepository;

  @Autowired
  public LoggerService(LoggerRepository loggerRepository) {
    this.loggerRepository = loggerRepository;
  }

  /**
   * Returns all logs recorded during system run.
   * 
   * @param pageSize
   * @return Page object containing all logs (up to page size)
   */
  public Page<Logger> getAllLogs(int pageSize) {
    Pageable paging = PageRequest.of(0, pageSize);
    return loggerRepository.findAll(paging);
  }

  /**
   * Gets all logs relevant to specified user.
   * 
   * @param username - Username used to fetch logs
   * @param pageSize - page size
   * @return Page object containing logs of specified user.
   */
  public Page<Logger> getByUserName(String username, int pageSize) {
    Pageable paging = PageRequest.of(0, pageSize);
    Page<Logger> results = loggerRepository.findByUserName(username, paging).orElseThrow(EntityMissingException::new);
    return results;

  }

  /**
   * User commands come from the user command files or from manual entries in the
   * students' web forms. Some params may not be needed depending on commands, use
   * "null" for those.
   * 
   * @param user
   * @param transactionNumber
   * @param commandType
   * @param stockSymbol
   * @param filename
   * @param funds
   * @return
   */
  public Logger createCommandLog(String user, Long transactionNumber, Enums.CommandType commandType, String stockSymbol,
      String filename, Double funds) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = createLog(Enums.LogType.UserCommandType, user, finalTransactionNum, commandType, stockSymbol, filename,
        funds, null);
    return loggerRepository.save(log);
  }

  /**
   * Every hit to the quote server requires a log entry with the results. The
   * price, symbol, username, timestamp and cryptokey are as returned by the quote
   * server.
   * 
   * @param user
   * @param transactionNumber
   * @param stockSymbol
   * @param unitPrice
   * @param quoteServerTime
   * @param cryptoKey
   * @return Saves newly created log to the logs repo.
   */
  public Logger createQuoteServerLog(String user, Long transactionNumber, String stockSymbol, Double unitPrice,
      Long quoteServerTime, String cryptoKey) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = new Logger(Enums.LogType.QuoteServerType, finalTransactionNum, "Pfilbert");
    log.setUserName(user);
    log.setStockSymbol(stockSymbol);
    log.setUnitPrice(unitPrice);
    log.setQuoteServerTime(quoteServerTime);
    log.setCryptoKey(cryptoKey);
    return loggerRepository.save(log);
  }

  /**
   * Any time a user's account is touched, an account message is printed.
   * Appropriate actions are "add" or "remove". Used anytime funds are added or
   * removed from account.
   * 
   * @param user              - Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be
   *                          consistent across all logs.
   * @param action            - "add" or "remove".
   * @param funds             - Amount being moved.
   * @return Saves the newly created log to the logs repo.
   */
  public Logger createAccountTransactionLog(String user, Long transactionNumber, String action, Double funds) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = new Logger(Enums.LogType.AccountTransactionType, finalTransactionNum, "Pfilbert");
    log.setUserName(user);
    log.setAction(action);
    log.setFunds(funds);
    return loggerRepository.save(log);
  }

  /**
   * System events can be current user commands, interserver communications, or
   * the execution of previously set triggers. For unused optional params, use
   * "null".
   * 
   * @param user              - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be
   *                          consistent across all logs.
   * @param commandType       - Command type if logging a valid command type.
   * @param stockSymbol       - OPTIONAL: Symbol of stock if relevant
   * @param filename          - OPTIONAL: Used for DUMPLOG commands
   * @param funds             - OPTIONAL: amount of money being moved
   * @return
   */
  public Logger createSystemEventLog(String user, Long transactionNumber, Enums.CommandType commandType,
      String stockSymbol, String filename, Double funds) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = createLog(Enums.LogType.SystemEventType, user, finalTransactionNum, commandType, stockSymbol, filename,
        funds, null);
    return loggerRepository.save(log);
  }

  /**
   * Error messages contain all the information of user commands, in addition to
   * an optional error message. For unused optional params, use "null".
   * 
   * @param user              - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be
   *                          consistent across all logs.
   * @param commandType       - Command type if logging a valid command type.
   * @param stockSymbol       - OPTIONAL: Symbol of stock if relevant
   * @param filename          - OPTIONAL: Used for DUMPLOG commands
   * @param funds             - OPTIONAL: amount of money being moved
   * @param errorMessage      - OPTIONAL: message relevant to event
   * @return
   */
  public Logger createErrorEventLog(String user, Long transactionNumber, Enums.CommandType commandType,
      String stockSymbol, String filename, Double funds, String errorMessage) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = createLog(Enums.LogType.ErrorEventType, user, finalTransactionNum, commandType, stockSymbol, filename,
        funds, errorMessage);
    return loggerRepository.save(log);
  }

  /**
   * Debugging messages contain all the information of user commands, in addition
   * to an optional debug message
   * 
   * @param user              - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be
   *                          consistent across all logs.
   * @param commandType       - Command type if logging a valid command type.
   * @param stockSymbol       - OPTIONAL: Symbol of stock if relevant
   * @param filename          - OPTIONAL: Used for DUMPLOG commands
   * @param funds             - OPTIONAL: amount of money being moved
   * @param debugMessage      - OPTIONAL: message relevant to event
   * @return
   */
  public Logger createDebugLog(String user, Long transactionNumber, Enums.CommandType commandType, String stockSymbol,
      String filename, Double funds, String debugMessage) {
    long finalTransactionNum = transactionNumber != null ? transactionNumber : 0;
    Logger log = createLog(Enums.LogType.DebugType, user, finalTransactionNum, commandType, stockSymbol, filename,
        funds, debugMessage);
    return loggerRepository.save(log);
  }

  private Logger createLog(Enums.LogType logType, String user, Long transactionNumber, Enums.CommandType commandType,
      String stockSymbol, String filename, Double funds, String message) {
    Logger log = new Logger(logType, transactionNumber, "Pfilbert");
    if (user != null)
      log.setUserName(user);
    if (commandType != null)
      log.setCommandType(commandType);
    if (stockSymbol != null)
      log.setStockSymbol(stockSymbol);
    if (filename != null)
      log.setFileName(filename);
    if (funds != null)
      log.setFunds(funds);
    if (message != null)
      log.setMessage(message);
    return log;
  }

}
