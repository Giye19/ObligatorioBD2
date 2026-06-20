package com.ucu.ticketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private String mail;
    private String rol;
    private String docPais;
    private String docTipo;
    private String docNumero;
    private String dirPais;
    private String dirLocalidad;
    private String dirCalle;
    private String dirNumero;
    private String dirCodPostal;
    private List<String> telefonos;

    // Campos opcionales según el rol (null si no aplica)
    private String fechaRegistro;       // Usuario General
    private String estadoVerificacion;  // Usuario General
    private String nroLegajo;           // Func Validacion
    private String fechaAsignacion;     // Adm Pais Sede
    private String nombrePais;          // Adm Pais Sede
}