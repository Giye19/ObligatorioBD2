package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private String mail;
    private String password;   // hash BCrypt, nunca se expone en DTOs de salida
    private String rol;        // ADMIN  FUNCIONARIO  USUARIO

    // documento (compuesto)
    private String docPais;
    private String docTipo;
    private String docNumero;

    // dirección (compuesta)
    private String dirPais;
    private String dirLocalidad;
    private String dirCalle;
    private String dirNumero;
    private String dirCodPostal;

    // lista de teléfonos asociados
    private List<String> telefonos;
}