package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncValidacion {

    private Long idFuncionario;
    private Long idUsuario;
    private String nroLegajo;
    private Boolean activo;
}