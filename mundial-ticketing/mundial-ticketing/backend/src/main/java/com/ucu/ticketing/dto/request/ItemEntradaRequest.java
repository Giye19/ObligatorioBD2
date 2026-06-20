package com.ucu.ticketing.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//representa un item dentro de una compra: un sector y la cantidad de entradas que se quieren para ese sector
@Data
public class ItemEntradaRequest {

    @NotBlank(message = "La letra del sector es obligatoria")
    private String letraSector;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}