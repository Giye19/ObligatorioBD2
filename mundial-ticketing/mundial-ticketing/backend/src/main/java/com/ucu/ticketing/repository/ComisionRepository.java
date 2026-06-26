package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Comision;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ComisionRepository {

    private final JdbcTemplate jdbc;

    public ComisionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Comision> rowMapper = (rs, rowNum) -> {
        Comision c = new Comision();
        c.setIdComision(rs.getLong("Id_Comision"));
        c.setFechaInicio(rs.getDate("Fecha_Inicio").toLocalDate());
        java.sql.Date fechaFin = rs.getDate("Fecha_Fin");
        c.setFechaFin(fechaFin != null ? fechaFin.toLocalDate() : null);
        c.setPorcentaje(rs.getBigDecimal("Porcentaje"));
        return c;
    };

    public Comision findVigente() {
        String sql = "select * from Comision " +
                     "where Fecha_Inicio <= curdate() " +
                     "and (Fecha_Fin is null or Fecha_Fin >= curdate()) " +
                     "order by Fecha_Inicio desc limit 1";
        List<Comision> resultado = jdbc.query(sql, rowMapper);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Comision findById(Long idComision) {
        String sql = "select * from Comision where Id_Comision = ?";
        List<Comision> resultado = jdbc.query(sql, rowMapper, idComision);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public void cerrarVigente() {
        String sql = "update Comision set Fecha_Fin = date_sub(curdate(), interval 1 day) " +
                     "where Fecha_Fin is null";
        jdbc.update(sql);
    }

    public void insert(java.math.BigDecimal porcentaje) {
        String sql = "insert into Comision (Fecha_Inicio, Fecha_Fin, Porcentaje) values (curdate(), null, ?)";
        jdbc.update(sql, porcentaje);
    }
}