package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.PaisSede;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Pais_Sede
@Repository
public class PaisSedeRepository {

    private final JdbcTemplate jdbc;

    public PaisSedeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<PaisSede> rowMapper = (rs, rowNum) -> {
        PaisSede p = new PaisSede();
        p.setNombrePais(rs.getString("nombre_pais"));
        return p;
    };

    // devuelve todos los paises sede registrados
    public List<PaisSede> findAll() {
        String sql = "select * from Pais_Sede";
        return jdbc.query(sql, rowMapper);
    }

    // verifica si existe un pais sede con ese nombre
    // se usa para validar referencias antes de insertar un estadio
    public boolean existsByNombre(String nombrePais) {
        String sql = "select count(*) from Pais_Sede where nombre_pais = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombrePais);
        return count != null && count > 0;
    }
}