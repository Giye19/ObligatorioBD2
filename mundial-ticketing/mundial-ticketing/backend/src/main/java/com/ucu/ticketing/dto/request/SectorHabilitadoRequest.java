package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


//representa un sector habilitado dentro de EventoRequest, con el costo de entrada para ese evento puntual.
 

@Data
public class SectorHabilitadoRequest {

    @NotBlank(message = "La letra del sector es obligatoria")
    private String letraSector;

    @NotNull(message = "El costo de entrada es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    private BigDecimal costoEntrada;
}