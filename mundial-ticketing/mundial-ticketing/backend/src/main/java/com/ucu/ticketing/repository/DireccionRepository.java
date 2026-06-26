package com.ucu.ticketing.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class DireccionRepository {

    private final JdbcTemplate jdbc;
    private final PaisRepository paisRepository;

    public DireccionRepository(JdbcTemplate jdbc, PaisRepository paisRepository) {
        this.jdbc = jdbc;
        this.paisRepository = paisRepository;
    }

    public Long insert(String nombrePais, String localidad, String calle,
                        String numeroDireccion, String codigoPostal) {

        Long idPais = paisRepository.insertSiNoExiste(nombrePais);

        String sql = "insert into Direccion (Id_Pais, Localidad, Calle, Numero_Direccion, Codigo_Postal) " +
                     "values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idPais);
            ps.setString(2, localidad);
            ps.setString(3, calle);
            ps.setString(4, numeroDireccion);
            ps.setString(5, codigoPostal);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}