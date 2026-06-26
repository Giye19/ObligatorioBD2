package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispositivo {

    private Long idDispositivo;
    private Long idFuncionario;
    private String codigoDispositivo;
    private Boolean activo;
}