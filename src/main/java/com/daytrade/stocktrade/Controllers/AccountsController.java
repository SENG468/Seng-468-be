package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Services.AccountService;
import com.daytrade.stocktrade.Services.SecurityService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

  private final AccountService accountService;
  private final SecurityService securityService;

  public AccountsController(AccountService accountService, SecurityService securityService) {
    this.accountService = accountService;
    this.securityService = securityService;
  }

  @GetMapping("/me")
  public Account getMyAccount(@RequestHeader("authorization") String authorization)
      throws EntityMissingException {
    String name = securityService.getUserFromJwt(authorization);
    return accountService.getByName(name);
  }

  @PostMapping("/add")
  public Account addFundsToAccount(@Valid @RequestBody Account account) {
    return accountService.addFundsToAccount(account);
  }
}
