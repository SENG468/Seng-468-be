package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Logger;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Services.LoggerService;
import com.daytrade.stocktrade.Services.SecurityService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LoggerController {

  private final LoggerService loggerService;
  private final SecurityService securityService;

  public LoggerController(LoggerService loggerService, SecurityService securityService) {
    this.loggerService = loggerService;
    this.securityService = securityService;
  }

  // This endpoint should only be able to be accessed by admin
  @GetMapping("/all")
  public Page<Logger> getAllLogs(@RequestHeader("authorization") String authorization,
      @RequestParam("page_size") int pageSize) {
    // TODO: Check caller is admin
    return loggerService.getAllLogs(pageSize);
  }

  // Also for admin, can access any particular user logs
  @GetMapping("/user/{userId}")
  public Page<Logger> getLogsByUser(@RequestHeader("authorization") String authorization,
      @PathVariable("userId") String userId, @RequestParam("page_size") int pageSize) throws EntityMissingException {
    // TODO: Check caller is admin
    return loggerService.getByUserName(userId, pageSize);
  }

  // Normal user can only access their own logs based of jwt
  @GetMapping("/user")
  public Page<Logger> getLogsForUser(@RequestHeader("authorization") String authorization,
      @RequestParam("page_size") int pageSize) throws EntityMissingException {
    String name = securityService.getUserFromJwt(authorization);
    return loggerService.getByUserName(name, pageSize);
  }
}
