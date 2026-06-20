package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Comision;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

// maneja el acceso a datos de la tabla Comision
// la comision puede variar a lo largo del tiempo, por eso siempre
// se busca la vigente al momento de la consulta, no un valor fijo
@Repository
public class ComisionRepository {

    private final JdbcTemplate jdbc;

    public ComisionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Comision> rowMapper = (rs, rowNum) -> {
        Comision c = new Comision();
        c.setIdComision(rs.getInt("id_comision"));
        c.setPorcentaje(rs.getBigDecimal("porcentaje"));
        c.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        java.sql.Date fechaFin = rs.getDate("fecha_fin");
        c.setFechaFin(fechaFin != null ? fechaFin.toLocalDate() : null);
        return c;
    };

    // busca la comision vigente a la fecha actual
    // vigente significa: fecha_inicio <= hoy y (fecha_fin es null o fecha_fin >= hoy)
    // devuelve null si por algun error de datos no hay ninguna vigente
    public Comision findVigente() {
        String sql = "select * from Comision " +
                     "where fecha_inicio <= curdate() " +
                     "and (fecha_fin is null or fecha_fin >= curdate()) " +
                     "order by fecha_inicio desc limit 1";
        List<Comision> resultado = jdbc.query(sql, rowMapper);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // busca una comision por id, se usa para mostrar el porcentaje
    // que se aplico a una venta ya realizada (historico, no el vigente)
    public Comision findById(Integer idComision) {
        String sql = "select * from Comision where id_comision = ?";
        List<Comision> resultado = jdbc.query(sql, rowMapper, idComision);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // cierra la comision vigente actual, poniendo fecha_fin = ayer
    // se llama justo antes de insertar una nueva comision, para que
    // no se superpongan dos comisiones vigentes al mismo tiempo
    public void cerrarVigente() {
        String sql = "update Comision set fecha_fin = date_sub(curdate(), interval 1 day) " +
                     "where fecha_fin is null";
        jdbc.update(sql);
    }

    // inserta una nueva comision vigente desde hoy
    public void insert(Comision comision) {
        String sql = "insert into Comision (porcentaje, fecha_inicio, fecha_fin) " +
                     "values (?, curdate(), null)";
        jdbc.update(sql, comision.getPorcentaje());
    }
}