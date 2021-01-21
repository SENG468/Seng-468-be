package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
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

  @PostMapping("/add-funds")
  public Account addFundsToAccount(
      @Valid @RequestBody Account account, @RequestHeader("authorization") String authorization) {
    String name = securityService.getUserFromJwt(authorization);
    if (!name.equals(account.getName())) {
      throw new BadRequestException("You can only add funds to an account you own");
    }
    return accountService.addFundsToAccount(account);
  }
}
