package com.daytrade.stocktrade.Models;

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

  @Id private String id;

  @NotNull
  @NotBlank
  @Indexed(unique = true)
  private String username;

  @Email @NotBlank private String email;

  @NotNull @NotBlank private String securityCode;

  @NotNull @NotBlank private String password;
}
