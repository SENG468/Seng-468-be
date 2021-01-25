package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Logger;
import com.daytrade.stocktrade.Services.LoggerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
