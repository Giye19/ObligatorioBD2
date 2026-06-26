package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Estadio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EstadioRepository {

    private final JdbcTemplate jdbc;

    public EstadioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE =
            "select e.Id_Estadio, e.Nombre_Estadio, e.Id_Pais_Sede, e.Id_Direccion, p.Nombre_Pais " +
            "from Estadio e " +
            "join Pais_Sede ps on ps.Id_Pais_Sede = e.Id_Pais_Sede " +
            "join Pais p on p.Id_Pais = ps.Id_Pais ";

    private final RowMapper<Estadio> rowMapper = (rs, rowNum) -> {
        Estadio e = new Estadio();
        e.setIdEstadio(rs.getLong("Id_Estadio"));
        e.setNombreEstadio(rs.getString("Nombre_Estadio"));
        e.setIdPaisSede(rs.getLong("Id_Pais_Sede"));
        e.setIdDireccion(rs.getLong("Id_Direccion"));
        e.setNombrePais(rs.getString("Nombre_Pais"));
        return e;
    };

    public List<Estadio> findAll() {
        return jdbc.query(SELECT_BASE, rowMapper);
    }

    public Estadio findById(Long idEstadio) {
        String sql = SELECT_BASE + "where e.Id_Estadio = ?";
        List<Estadio> resultado = jdbc.query(sql, rowMapper, idEstadio);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Estadio> findByPaisSede(Long idPaisSede) {
        String sql = SELECT_BASE + "where e.Id_Pais_Sede = ?";
        return jdbc.query(sql, rowMapper, idPaisSede);
    }

    public Long insert(String nombreEstadio, Long idPaisSede, Long idDireccion) {
        String sql = "insert into Estadio (Nombre_Estadio, Id_Pais_Sede, Id_Direccion) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombreEstadio);
            ps.setLong(2, idPaisSede);
            ps.setLong(3, idDireccion);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}