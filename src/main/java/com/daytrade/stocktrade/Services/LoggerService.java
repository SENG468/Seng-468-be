package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.LogRequest;
import com.daytrade.stocktrade.Models.Logger;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Repositories.LoggerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
   * @return Page object containing all logs (up to page size)F
   */
  public Page<Logger> getAllLogs(Pageable page) {
    return loggerRepository.findAll(page);
  }

  /**
   * Gets all logs relevant to specified user.
   *
   * @param username - Username used to fetch logs
   * @param pageSize - page size
   * @return Page object containing logs of specified user.
   */
  public Page<Logger> getByUserName(String username, Pageable page) {
    Page<Logger> results =
        loggerRepository.findByUserName(username, page).orElseThrow(EntityMissingException::new);
    return results;
  }

  public StreamingResponseBody generateLogFile(LogRequest request)
      throws ParserConfigurationException, TransformerException {
    createCommandLog(
        request.getUsername(),
        request.getTransactionId(),
        Enums.CommandType.DUMPLOG,
        null,
        request.getFilename(),
        null);
    List<Logger> results = getLogs(request.getUsername());
    ListIterator<Logger> resultIterator = results.listIterator();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    Element root = doc.createElement("log");

    doc.appendChild(root);

    try {
      while (resultIterator.hasNext()) {
        root.appendChild(createLogElement(doc, resultIterator.next()));
      }
    } catch (Exception e) {
      throw new EntityMissingException();
    }

    return new StreamingResponseBody() {
      @Override
      public void writeTo(OutputStream out) throws IOException {
        try {
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");
          transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

          StreamResult result = new StreamResult(out);
          DOMSource source = new DOMSource(doc);
          transformer.transform(source, result);
          out.flush();
        } catch (Exception e) {
          createErrorEventLog(
              request.getUsername(),
              request.getTransactionId(),
              Enums.CommandType.DUMPLOG,
              null,
              request.getFilename(),
              null,
              "Logfile generation error.");
        }
      }
    };
  }

  /** Wrapper for createErrorEventLog */
  public Logger createTransactionErrorLog(
      Transaction transaction, Enums.CommandType cmdType, String message) {
    return createErrorEventLog(
        transaction.getUserName(),
        transaction.getTransactionId(),
        cmdType,
        transaction.getStockCode(),
        null,
        null,
        message);
  }

  /** Wrapper for createCommandLog */
  public Logger createTransactionCommandLog(
      Transaction transaction, Enums.CommandType cmdType, String stockSymbol) {
    return createCommandLog(
        transaction.getUserName(),
        transaction.getTransactionId(),
        cmdType,
        stockSymbol == null ? transaction.getStockCode() : stockSymbol,
        null,
        transaction.getCashAmount());
  }

  /** Wrapper for createSystemEventLog */
  public Logger createTransactionSysEventLog(
      Transaction transaction, Enums.CommandType cmdType, String stockSymbol) {
    return createSystemEventLog(
        transaction.getUserName(),
        transaction.getTransactionId(),
        cmdType,
        stockSymbol == null ? transaction.getStockCode() : stockSymbol,
        null,
        transaction.getCashAmount());
  }

  /**
   * User commands come from the user command files or from manual entries in the students' web
   * forms. Some params may not be needed depending on commands, use "null" for those.
   *
   * @param user
   * @param transactionNumber
   * @param commandType
   * @param stockSymbol
   * @param filename
   * @param funds
   * @return
   */
  public Logger createCommandLog(
      String user,
      String transactionNumber,
      Enums.CommandType commandType,
      String stockSymbol,
      String filename,
      Double funds) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log =
        createLog(
            Enums.LogType.UserCommandType,
            user,
            finalTransactionNum,
            commandType,
            stockSymbol,
            filename,
            funds,
            null);
    return loggerRepository.save(log);
  }

  /**
   * Every hit to the quote server requires a log entry with the results. The price, symbol,
   * username, timestamp and cryptokey are as returned by the quote server.
   *
   * @param user
   * @param transactionNumber
   * @param stockSymbol
   * @param unitPrice
   * @param quoteServerTime
   * @param cryptoKey
   * @return Saves newly created log to the logs repo.
   */
  public Logger createQuoteServerLog(
      String user,
      String transactionNumber,
      String stockSymbol,
      Double unitPrice,
      Instant quoteServerTime,
      String cryptoKey) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log = new Logger(Enums.LogType.QuoteServerType, finalTransactionNum, "Pfilbert");
    log.setUserName(user);
    log.setStockSymbol(stockSymbol);
    log.setUnitPrice(unitPrice);
    log.setQuoteServerTime(quoteServerTime);
    log.setCryptoKey(cryptoKey);
    return loggerRepository.save(log);
  }

  /**
   * Any time a user's account is touched, an account message is printed. Appropriate actions are
   * "add" or "remove". Used anytime funds are added or removed from account.
   *
   * @param user - Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be consistent across
   *     all logs.
   * @param action - "add" or "remove".
   * @param funds - Amount being moved.
   * @return Saves the newly created log to the logs repo.
   */
  public Logger createAccountTransactionLog(
      String user, String transactionNumber, String action, Double funds) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log = new Logger(Enums.LogType.AccountTransactionType, finalTransactionNum, "Pfilbert");
    log.setUserName(user);
    log.setAction(action);
    log.setFunds(funds);
    return loggerRepository.save(log);
  }

  /**
   * System events can be current user commands, interserver communications, or the execution of
   * previously set triggers. For unused optional params, use "null".
   *
   * @param user - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be consistent across
   *     all logs.
   * @param commandType - Command type if logging a valid command type.
   * @param stockSymbol - OPTIONAL: Symbol of stock if relevant
   * @param filename - OPTIONAL: Used for DUMPLOG commands
   * @param funds - OPTIONAL: amount of money being moved
   * @return
   */
  public Logger createSystemEventLog(
      String user,
      String transactionNumber,
      Enums.CommandType commandType,
      String stockSymbol,
      String filename,
      Double funds) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log =
        createLog(
            Enums.LogType.SystemEventType,
            user,
            finalTransactionNum,
            commandType,
            stockSymbol,
            filename,
            funds,
            null);
    return loggerRepository.save(log);
  }

  /**
   * Error messages contain all the information of user commands, in addition to an optional error
   * message. For unused optional params, use "null".
   *
   * @param user - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be consistent across
   *     all logs.
   * @param commandType - Command type if logging a valid command type.
   * @param stockSymbol - OPTIONAL: Symbol of stock if relevant
   * @param filename - OPTIONAL: Used for DUMPLOG commands
   * @param funds - OPTIONAL: amount of money being moved
   * @param errorMessage - OPTIONAL: message relevant to event
   * @return
   */
  public Logger createErrorEventLog(
      String user,
      String transactionNumber,
      Enums.CommandType commandType,
      String stockSymbol,
      String filename,
      Double funds,
      String errorMessage) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log =
        createLog(
            Enums.LogType.ErrorEventType,
            user,
            finalTransactionNum,
            commandType,
            stockSymbol,
            filename,
            funds,
            errorMessage);
    try {
      System.out.print(new ObjectMapper().writeValueAsString(log));
    } catch (Exception ex) {
    }
    return loggerRepository.save(log);
  }

  /**
   * Debugging messages contain all the information of user commands, in addition to an optional
   * debug message
   *
   * @param user - OPTIONAL: Username of user performing transaction.
   * @param transactionNumber - Transaction number of this transaction, should be consistent across
   *     all logs.
   * @param commandType - Command type if logging a valid command type.
   * @param stockSymbol - OPTIONAL: Symbol of stock if relevant
   * @param filename - OPTIONAL: Used for DUMPLOG commands
   * @param funds - OPTIONAL: amount of money being moved
   * @param debugMessage - OPTIONAL: message relevant to event
   * @return
   */
  public Logger createDebugLog(
      String user,
      String transactionNumber,
      Enums.CommandType commandType,
      String stockSymbol,
      String filename,
      Double funds,
      String debugMessage) {
    String finalTransactionNum = transactionNumber != null ? transactionNumber : "1";
    Logger log =
        createLog(
            Enums.LogType.DebugType,
            user,
            finalTransactionNum,
            commandType,
            stockSymbol,
            filename,
            funds,
            debugMessage);
    return loggerRepository.save(log);
  }

  private Logger createLog(
      Enums.LogType logType,
      String user,
      String transactionNumber,
      Enums.CommandType commandType,
      String stockSymbol,
      String filename,
      Double funds,
      String message) {
    Logger log = new Logger(logType, transactionNumber, "Pfilbert");
    if (user != null) log.setUserName(user);
    if (commandType != null) log.setCommandType(commandType);
    if (stockSymbol != null) log.setStockSymbol(stockSymbol);
    if (filename != null) log.setFileName(filename);
    if (funds != null) log.setFunds(funds);
    if (message != null) log.setMessage(message);
    return log;
  }

  private List<Logger> getLogs(String username) {
    List<Logger> results = new ArrayList<>();
    try {
      Page<Logger> logs =
          username == null
              ? loggerRepository.findAll(PageRequest.of(0, 5000))
              : loggerRepository
                  .findByUserName(username, PageRequest.of(0, 5000))
                  .orElseThrow(EntityMissingException::new);
      List<Logger> content = new ArrayList<>(logs.getContent());
      while (logs.hasNext()) {
        Page<Logger> nextLogs =
            username == null
                ? loggerRepository.findAll(logs.nextPageable())
                : loggerRepository
                    .findByUserName(username, logs.nextPageable())
                    .orElseThrow(EntityMissingException::new);
        content.addAll(nextLogs.getContent());
        logs = nextLogs;
      }
      results = content;
    } catch (Exception e) {
      throw new EntityMissingException();
    }
    return results;
  }

  private static Node createLogElement(Document doc, Logger log) {
    Element logElem;
    switch (log.getLogType()) {
      case UserCommandType:
        logElem = doc.createElement("userCommand");
        commonElements(doc, logElem, log, true);
        break;
      case QuoteServerType:
        logElem = doc.createElement("quoteServer");
        commonElements(doc, logElem, log, false);
        logElem.appendChild(
            createLogElement(doc, "price", String.format("%.2f", log.getUnitPrice())));
        logElem.appendChild(createLogElement(doc, "username", log.getUserName()));
        logElem.appendChild(createLogElement(doc, "stockSymbol", log.getStockSymbol()));
        logElem.appendChild(
            createLogElement(
                doc, "quoteServerTime", Long.toString(log.getQuoteServerTime().toEpochMilli())));
        logElem.appendChild(createLogElement(doc, "cryptokey", log.getCryptoKey()));
        break;
      case AccountTransactionType:
        logElem = doc.createElement("accountTransaction");
        commonElements(doc, logElem, log, false);
        logElem.appendChild(createLogElement(doc, "action", log.getAction()));
        logElem.appendChild(createLogElement(doc, "username", log.getUserName()));
        logElem.appendChild(createLogElement(doc, "funds", String.format("%.2f", log.getFunds())));
        break;
      case SystemEventType:
        logElem = doc.createElement("systemEvent");
        commonElements(doc, logElem, log, true);
        break;
      case ErrorEventType:
        logElem = doc.createElement("errorEvent");
        commonElements(doc, logElem, log, true);
        if (log.getMessage() != null)
          logElem.appendChild(createLogElement(doc, "errorMessage", log.getMessage()));
        break;
      case DebugType:
        logElem = doc.createElement("debugEvent");
        commonElements(doc, logElem, log, true);
        if (log.getMessage() != null)
          logElem.appendChild(createLogElement(doc, "debugMessage", log.getMessage()));
        break;
      default:
        logElem = doc.createElement("errorEvent");
        commonElements(doc, logElem, log, true);
        if (log.getMessage() != null)
          logElem.appendChild(
              createLogElement(doc, "errorMessage", "Logging Error - Invalid Log Type"));
    }
    return logElem;
  }

  private static void commonElements(
      Document doc, Element logElem, Logger log, Boolean semiCommon) {
    logElem.appendChild(
        createLogElement(doc, "timestamp", Long.toString(log.getTimestamp().toEpochMilli())));
    logElem.appendChild(createLogElement(doc, "server", log.getServerName()));
    logElem.appendChild(createLogElement(doc, "transactionNum", log.getTransactionNumber()));
    if (semiCommon) {
      logElem.appendChild(createLogElement(doc, "command", log.getCommandType().name()));
      if (log.getUserName() != null)
        logElem.appendChild(createLogElement(doc, "username", log.getUserName()));
      if (log.getStockSymbol() != null)
        logElem.appendChild(createLogElement(doc, "stockSymbol", log.getStockSymbol()));
      if (log.getFileName() != null)
        logElem.appendChild(createLogElement(doc, "filename", log.getFileName()));
      if (log.getFunds() != null)
        logElem.appendChild(createLogElement(doc, "funds", String.format("%.2f", log.getFunds())));
    }
  }

  private static Node createLogElement(Document doc, String name, String value) {

    Element node = doc.createElement(name);
    node.appendChild(doc.createTextNode(value));

    return node;
  }
}
