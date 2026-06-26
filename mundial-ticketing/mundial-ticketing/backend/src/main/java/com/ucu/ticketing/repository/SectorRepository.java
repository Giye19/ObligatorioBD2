package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Sector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SectorRepository {

    private final JdbcTemplate jdbc;

    public SectorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Sector> rowMapper = (rs, rowNum) -> {
        Sector s = new Sector();
        s.setIdSector(rs.getLong("Id_Sector"));
        s.setIdEstadio(rs.getLong("Id_Estadio"));
        s.setLetra(rs.getString("Letra"));
        s.setCapacidad(rs.getInt("Capacidad"));
        return s;
    };

    public List<Sector> findByEstadio(Long idEstadio) {
        String sql = "select * from Sector where Id_Estadio = ? order by Letra";
        return jdbc.query(sql, rowMapper, idEstadio);
    }

    public Sector findByEstadioYLetra(Long idEstadio, String letra) {
        String sql = "select * from Sector where Id_Estadio = ? and Letra = ?";
        List<Sector> resultado = jdbc.query(sql, rowMapper, idEstadio, letra);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Sector findById(Long idSector) {
        String sql = "select * from Sector where Id_Sector = ?";
        List<Sector> resultado = jdbc.query(sql, rowMapper, idSector);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existsByEstadioYLetra(Long idEstadio, String letra) {
        return findByEstadioYLetra(idEstadio, letra) != null;
    }

    public Long insert(Long idEstadio, String letra, Integer capacidad) {
        String sql = "insert into Sector (Id_Estadio, Letra, Capacidad) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEstadio);
            ps.setString(2, letra);
            ps.setInt(3, capacidad);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}