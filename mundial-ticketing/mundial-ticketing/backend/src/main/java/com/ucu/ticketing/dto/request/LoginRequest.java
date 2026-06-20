package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequest {

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail debe tener formato válido")
    private String mail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}