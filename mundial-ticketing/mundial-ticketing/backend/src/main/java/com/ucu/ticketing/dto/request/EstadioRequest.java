package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;



//para crear nuevo estadio
//solo accesible por admin

@Data
public class EstadioRequest {

    @NotBlank(message = "El nombre del estadio es obligatorio")
    private String nombre;

    @NotBlank(message = "El país sede es obligatorio")
    private String nombrePais;
}