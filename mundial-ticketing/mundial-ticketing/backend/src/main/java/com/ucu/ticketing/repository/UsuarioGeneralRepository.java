package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.UsuarioGeneral;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UsuarioGeneralRepository {

    private final JdbcTemplate jdbc;

    public UsuarioGeneralRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<UsuarioGeneral> rowMapper = (rs, rowNum) -> {
        UsuarioGeneral ug = new UsuarioGeneral();
        ug.setIdUsuarioGeneral(rs.getLong("Id_Usuario_General"));
        ug.setIdUsuario(rs.getLong("Id_Usuario"));
        ug.setFechaRegistro(rs.getTimestamp("Fecha_Registro").toLocalDateTime());
        ug.setEstadoVerificacion(rs.getString("Estado_Verificacion"));
        return ug;
    };

    public UsuarioGeneral findByIdUsuario(Long idUsuario) {
        String sql = "select * from Usuario_General where Id_Usuario = ?";
        List<UsuarioGeneral> resultado = jdbc.query(sql, rowMapper, idUsuario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Long insert(Long idUsuario) {
        String sql = "insert into Usuario_General (Id_Usuario, Estado_Verificacion) values (?, 'PENDIENTE')";
        jdbc.update(sql, idUsuario);

        return findByIdUsuario(idUsuario).getIdUsuarioGeneral();
    }

    public Long findIdUsuarioById(Long idUsuarioGeneral) {
        String sql = "select Id_Usuario from Usuario_General where Id_Usuario_General = ?";
        List<Long> resultado = jdbc.queryForList(sql, Long.class, idUsuarioGeneral);
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}