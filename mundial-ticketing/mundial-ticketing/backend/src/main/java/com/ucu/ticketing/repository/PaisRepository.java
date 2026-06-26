package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Pais;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PaisRepository {

    private final JdbcTemplate jdbc;

    public PaisRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Pais> rowMapper = (rs, rowNum) -> {
        Pais p = new Pais();
        p.setIdPais(rs.getLong("Id_Pais"));
        p.setNombrePais(rs.getString("Nombre_Pais"));
        return p;
    };

    public List<Pais> findAll() {
        String sql = "select * from Pais order by Nombre_Pais";
        return jdbc.query(sql, rowMapper);
    }

    public Pais findByNombre(String nombrePais) {
        String sql = "select * from Pais where Nombre_Pais = ?";
        List<Pais> resultado = jdbc.query(sql, rowMapper, nombrePais);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Pais findById(Long idPais) {
        String sql = "select * from Pais where Id_Pais = ?";
        List<Pais> resultado = jdbc.query(sql, rowMapper, idPais);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Long insertSiNoExiste(String nombrePais) {
        Pais existente = findByNombre(nombrePais);
        if (existente != null) {
            return existente.getIdPais();
        }

        String sql = "insert into Pais (Nombre_Pais) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombrePais);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}