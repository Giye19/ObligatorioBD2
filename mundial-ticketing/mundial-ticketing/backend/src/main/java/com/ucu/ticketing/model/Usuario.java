package com.ucu.ticketing.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    private Long idDocumento;
    private Long idDireccion;
    private String mail;
    private String password;
    private String nombre;
    private String apellido;
    private LocalDateTime fechaCreacion;

    private List<String> telefonos;
}