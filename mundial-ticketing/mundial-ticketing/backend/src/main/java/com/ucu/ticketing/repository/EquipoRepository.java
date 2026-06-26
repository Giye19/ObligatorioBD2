package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Equipo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EquipoRepository {

    private final JdbcTemplate jdbc;

    public EquipoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Equipo> rowMapper = (rs, rowNum) -> {
        Equipo e = new Equipo();
        e.setIdEquipo(rs.getLong("Id_Equipo"));
        e.setNombre(rs.getString("Nombre"));
        return e;
    };

    public List<Equipo> findAll() {
        String sql = "select * from Equipo order by Nombre";
        return jdbc.query(sql, rowMapper);
    }

    public Equipo findByNombre(String nombre) {
        String sql = "select * from Equipo where Nombre = ?";
        List<Equipo> resultado = jdbc.query(sql, rowMapper, nombre);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Equipo findById(Long idEquipo) {
        String sql = "select * from Equipo where Id_Equipo = ?";
        List<Equipo> resultado = jdbc.query(sql, rowMapper, idEquipo);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existsByNombre(String nombre) {
        return findByNombre(nombre) != null;
    }

    public Long insert(String nombre) {
        String sql = "insert into Equipo (Nombre) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}