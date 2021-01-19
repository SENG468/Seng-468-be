package com.daytrade.stocktrade.Models;

import lombok.Data;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Document(collection = "users")
@Data
public class User {

    @Id
    private Long id;

    @NotNull
    @NotBlank
    @Indexed(unique = true)
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String password;
}
