package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


// el mailDestino debe corresponder a un Usuario_General existente
@Data
public class TransferenciaRequest {

    @NotNull(message = "La entrada es obligatoria")
    private Long idEntrada;

    @NotBlank(message = "El mail destino es obligatorio")
    @Email(message = "El mail destino debe tener formato valido")
    private String mailDestino;
}