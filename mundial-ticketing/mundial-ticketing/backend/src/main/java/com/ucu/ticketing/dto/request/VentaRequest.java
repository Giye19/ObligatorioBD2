package com.ucu.ticketing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

// datos para realizar una compra de entradas
// la validacion de maximo 5 entradas totales se hace en el service y no aca porque depende de sumar las cantidades de todos los items
@Data
public class VentaRequest {

    @NotNull(message = "El evento es obligatorio")
    private Integer idEvento;

    @NotEmpty(message = "Debe incluir al menos un item de compra")
    @Size(max = 5, message = "No se pueden incluir mas de 5 items distintos en una compra")
    @Valid
    private List<ItemEntradaRequest> itemsEntrada;
}