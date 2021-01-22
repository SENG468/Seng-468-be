package com.daytrade.stocktrade.Models.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EntityMissingException extends RuntimeException {
  public EntityMissingException() {
    super("Requested Object was not found");
  }
}
