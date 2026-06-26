package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingreso {

    private Long idValidacion;
    private Long idQr;
    private Long idDispositivo;
    private Long idFuncionario;
    private LocalDateTime fechaHoraIngreso;
    private String puertaIngreso;
}