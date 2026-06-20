package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//datos enviados por el dispositivo de escaneo
// el idDispositivo permite identificar al funcionario responsable
@Data
public class ValidacionRequest {

    @NotBlank(message = "El token del qr escaneado es obligatorio")
    private String tokenQr;

    @NotBlank(message = "El id del dispositivo es obligatorio")
    private String idDispositivo;

    private String puertaIngreso; // opcional
}