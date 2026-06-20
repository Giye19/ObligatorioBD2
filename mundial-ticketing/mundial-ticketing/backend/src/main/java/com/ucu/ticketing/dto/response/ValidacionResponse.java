package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionResponse {

    private boolean accesoPermitido;
    private String mensaje;
    private Integer idEntrada;
    private String letraSector;
    private String mailPropietario;
    private LocalDateTime horaIngreso;
}