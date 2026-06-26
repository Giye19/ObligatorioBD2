package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.AdmPaisSede;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdmPaisSedeRepository {

    private final JdbcTemplate jdbc;

    public AdmPaisSedeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<AdmPaisSede> rowMapper = (rs, rowNum) -> {
        AdmPaisSede a = new AdmPaisSede();
        a.setIdAdmin(rs.getLong("Id_Admin"));
        a.setIdUsuario(rs.getLong("Id_Usuario"));
        a.setIdPaisSede(rs.getLong("Id_Pais_Sede"));
        a.setFechaAsignacion(rs.getDate("Fecha_Asignacion").toLocalDate());
        a.setActivo(rs.getBoolean("Activo"));
        return a;
    };

    public AdmPaisSede findByIdUsuario(Long idUsuario) {
        String sql = "select * from Adm_Pais_Sede where Id_Usuario = ?";
        List<AdmPaisSede> resultado = jdbc.query(sql, rowMapper, idUsuario);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public AdmPaisSede findById(Long idAdmin) {
        String sql = "select * from Adm_Pais_Sede where Id_Admin = ?";
        List<AdmPaisSede> resultado = jdbc.query(sql, rowMapper, idAdmin);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean tieneJurisdiccion(Long idUsuarioAdmin, Long idPaisSede) {
        String sql = "select count(*) from Adm_Pais_Sede where Id_Usuario = ? and Id_Pais_Sede = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, idUsuarioAdmin, idPaisSede);
        return count != null && count > 0;
    }
}