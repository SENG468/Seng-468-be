package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.LogRequest;
import com.daytrade.stocktrade.Models.Logger;
import com.daytrade.stocktrade.Services.LoggerService;
import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/logs")
public class LoggerController {

  private final LoggerService loggerService;

  public LoggerController(LoggerService loggerService) {
    this.loggerService = loggerService;
  }

  // This endpoint should only be able to be accessed by admin
  @GetMapping("/all")
  public Page<Logger> getAllLogs(@PageableDefault(size = 2000) Pageable page) {
    // TODO: Check caller is admin
    return loggerService.getAllLogs(page);
  }

  // Also for admin, can access any particular user logs
  @GetMapping("/user/{userId}")
  public Page<Logger> getLogsByUser(
      @PathVariable("userId") String userId, @PageableDefault(size = 2000) Pageable page)
      throws EntityMissingException {
    // TODO: Check caller is admin
    return loggerService.getByUserName(userId, page);
  }

  // Normal user can only access their own logs based of jwt
  @GetMapping("/user")
  public Page<Logger> getLogsForUser(@PageableDefault(size = 200) Pageable page)
      throws EntityMissingException {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    return loggerService.getByUserName(name, page);
  }

  // Returns xml file containing all logs if username is null, otherwise returns xml file with
  // specified uesr logs
  @PostMapping("/dumplog")
  public ResponseEntity<StreamingResponseBody> getAllLogfile(
      @Valid @RequestBody LogRequest newLogRequest)
      throws ParserConfigurationException, TransformerException {
    newLogRequest.setUsername(newLogRequest.username.equals("") ? null : newLogRequest.username);
    StreamingResponseBody resource = loggerService.generateLogFile(newLogRequest);

    String formattedFilename = String.format("attachment; filename=%s.xml", newLogRequest.filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, formattedFilename);

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

  // Returns xml file of logs relevant to current user based off jwt
  @PostMapping("/user/dumplog")
  public ResponseEntity<StreamingResponseBody> getLogfileForUser(
      @Valid @RequestBody LogRequest newLogRequest)
      throws ParserConfigurationException, TransformerException {
    newLogRequest.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    StreamingResponseBody resource = loggerService.generateLogFile(newLogRequest);

    String formattedFilename = String.format("attachment; filename=%s.xml", newLogRequest.filename);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, formattedFilename);

    return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }
}
