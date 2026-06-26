package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {

    private Long idDireccion;
    private Long idPais;
    private String nombrePais;
    private String localidad;
    private String calle;
    private String numeroDireccion;
    private String codigoPostal;
}