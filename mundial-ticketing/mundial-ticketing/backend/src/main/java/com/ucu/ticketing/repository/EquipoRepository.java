package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Equipo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Equipo
@Repository
public class EquipoRepository {

    private final JdbcTemplate jdbc;

    public EquipoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Equipo> rowMapper = (rs, rowNum) -> {
        Equipo e = new Equipo();
        e.setNombre(rs.getString("nombre"));
        return e;
    };

    // devuelve todos los equipos registrados, ordenados alfabeticamente
    public List<Equipo> findAll() {
        String sql = "select * from Equipo order by nombre";
        return jdbc.query(sql, rowMapper);
    }

    // verifica si existe un equipo con ese nombre
    // se usa al crear un evento, para validar que los equipos
    // local y visitante existan antes de vincularlos
    public boolean existsByNombre(String nombre) {
        String sql = "select count(*) from Equipo where nombre = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombre);
        return count != null && count > 0;
    }

    // inserta un nuevo equipo
    public void insert(String nombre) {
        String sql = "insert into Equipo (nombre) values (?)";
        jdbc.update(sql, nombre);
    }
}