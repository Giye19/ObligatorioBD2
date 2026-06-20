package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


//tabla Func_Sector_Asignado
//se usa para validar la regla de que un funcionario debe haber validado entradas en todos los sectores que fue asignado

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncSectorAsignado {

    private String mailFuncionario;
    private Integer idEstadio;
    private String letraSector;
}