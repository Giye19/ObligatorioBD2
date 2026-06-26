package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.PaisSede;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaisSedeRepository {

    private final JdbcTemplate jdbc;

    public PaisSedeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<PaisSede> rowMapper = (rs, rowNum) -> {
        PaisSede ps = new PaisSede();
        ps.setIdPaisSede(rs.getLong("Id_Pais_Sede"));
        ps.setIdPais(rs.getLong("Id_Pais"));
        ps.setNombrePais(rs.getString("Nombre_Pais"));
        return ps;
    };

    private static final String SELECT_BASE =
            "select psede.Id_Pais_Sede, psede.Id_Pais, p.Nombre_Pais " +
            "from Pais_Sede psede " +
            "join Pais p on p.Id_Pais = psede.Id_Pais ";

    public List<PaisSede> findAll() {
        return jdbc.query(SELECT_BASE, rowMapper);
    }

    public PaisSede findById(Long idPaisSede) {
        String sql = SELECT_BASE + "where psede.Id_Pais_Sede = ?";
        List<PaisSede> resultado = jdbc.query(sql, rowMapper, idPaisSede);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public PaisSede findByNombrePais(String nombrePais) {
        String sql = SELECT_BASE + "where p.Nombre_Pais = ?";
        List<PaisSede> resultado = jdbc.query(sql, rowMapper, nombrePais);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existsByNombre(String nombrePais) {
        return findByNombrePais(nombrePais) != null;
    }
}