package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.UsuarioGeneral;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

// maneja el acceso a datos de la tabla Usuario_General
// (especializacion de Usuario)
@Repository
public class UsuarioGeneralRepository {

    private final JdbcTemplate jdbc;

    public UsuarioGeneralRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<UsuarioGeneral> rowMapper = (rs, rowNum) -> {
        UsuarioGeneral ug = new UsuarioGeneral();
        ug.setMail(rs.getString("mail"));
        ug.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        ug.setEstadoVerificacion(rs.getString("estado_verificacion"));
        return ug;
    };

    // busca los datos de usuario general por mail
    // devuelve null si el usuario no tiene este rol
    public UsuarioGeneral findByMail(String mail) {
        String sql = "select * from Usuario_General where mail = ?";
        List<UsuarioGeneral> resultado = jdbc.query(sql, rowMapper, mail);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta la especializacion de usuario general
    // se llama justo despues de insertar en Usuario (en el service,
    // dentro de la misma operacion de registro)
    public void insert(UsuarioGeneral usuarioGeneral) {
        String sql = "insert into Usuario_General (mail, fecha_registro, estado_verificacion) " +
                     "values (?, ?, ?)";
        jdbc.update(sql,
                usuarioGeneral.getMail(),
                Date.valueOf(usuarioGeneral.getFechaRegistro()),
                usuarioGeneral.getEstadoVerificacion());
    }

    // actualiza el estado de verificacion de identidad de un usuario
    public void updateEstadoVerificacion(String mail, String nuevoEstado) {
        String sql = "update Usuario_General set estado_verificacion = ? where mail = ?";
        jdbc.update(sql, nuevoEstado, mail);
    }
}