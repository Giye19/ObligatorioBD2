package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.AdmPaisSede;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

// maneja el acceso a datos de la tabla Adm_Pais_Sede
// (especializacion de Usuario)
@Repository
public class AdmPaisSedeRepository {

    private final JdbcTemplate jdbc;

    public AdmPaisSedeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<AdmPaisSede> rowMapper = (rs, rowNum) -> {
        AdmPaisSede adm = new AdmPaisSede();
        adm.setMail(rs.getString("mail"));
        adm.setFechaAsignacion(rs.getDate("fecha_asignacion").toLocalDate());
        adm.setNombrePais(rs.getString("nombre_pais"));
        return adm;
    };

    // busca un administrador por mail
    // devuelve null si el usuario no tiene este rol
    public AdmPaisSede findByMail(String mail) {
        String sql = "select * from Adm_Pais_Sede where mail = ?";
        List<AdmPaisSede> resultado = jdbc.query(sql, rowMapper, mail);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta la especializacion de administrador
    public void insert(AdmPaisSede admPaisSede) {
        String sql = "insert into Adm_Pais_Sede (mail, fecha_asignacion, nombre_pais) " +
                     "values (?, ?, ?)";
        jdbc.update(sql,
                admPaisSede.getMail(),
                Date.valueOf(admPaisSede.getFechaAsignacion()),
                admPaisSede.getNombrePais());
    }

    // verifica si un administrador tiene jurisdiccion sobre un pais
    // se usa para controlar que solo gestione estadios/eventos de su pais
    public boolean tieneJurisdiccion(String mailAdmin, String nombrePais) {
        String sql = "select count(*) from Adm_Pais_Sede " +
                     "where mail = ? and nombre_pais = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mailAdmin, nombrePais);
        return count != null && count > 0;
    }
}