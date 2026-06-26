package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstadioRequest {

    @NotBlank(message = "El nombre del estadio es obligatorio")
    private String nombre;

    @NotBlank(message = "El país sede es obligatorio")
    private String nombrePais;

    @NotBlank(message = "El país de la dirección es obligatorio")
    private String dirPais;

    @NotBlank(message = "La localidad es obligatoria")
    private String dirLocalidad;

    @NotBlank(message = "La calle es obligatoria")
    private String dirCalle;

    @NotBlank(message = "El número de dirección es obligatorio")
    private String dirNumero;

    @NotBlank(message = "El código postal es obligatorio")
    private String dirCodPostal;
}