package com.daytrade.stocktrade.Models;

import java.math.BigInteger;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
public class User {

  @Id private BigInteger id;

  @NotNull
  @NotBlank
  @Indexed(unique = true)
  private String username;

  @Email @NotBlank private String email;

  private String securityCode;

  @NotNull @NotBlank private String password;
}
