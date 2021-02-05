package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Services.AccountService;
import com.daytrade.stocktrade.Services.LoggerService;
import com.daytrade.stocktrade.Services.SecurityService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

  private final AccountService accountService;
  private final SecurityService securityService;
  private final LoggerService loggerService;

  public AccountsController(
      AccountService accountService, SecurityService securityService, LoggerService loggerService) {
    this.accountService = accountService;
    this.securityService = securityService;
    this.loggerService = loggerService;
  }

  @GetMapping("/me")
  public Account getMyAccount(@RequestHeader("authorization") String authorization)
      throws EntityMissingException {
    String name = securityService.getUserFromJwt(authorization);
    return accountService.getByName(name);
  }

  @PostMapping("/add")
  public Account addFundsToAccount(@Valid @RequestBody Account account) {
    loggerService.createCommandLog(
        account.getName(),
        account.getId(),
        Enums.CommandType.ADD,
        null,
        null,
        account.getBalance());
    return accountService.addFundsToAccount(account);
  }
}
