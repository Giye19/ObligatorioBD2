package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Ingreso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class IngresoRepository {

    private final JdbcTemplate jdbc;

    public IngresoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Ingreso> rowMapper = (rs, rowNum) -> {
        Ingreso i = new Ingreso();
        i.setIdValidacion(rs.getLong("Id_Validacion"));
        i.setIdQr(rs.getLong("Id_QR"));
        i.setIdDispositivo(rs.getLong("Id_Dispositivo"));
        i.setIdFuncionario(rs.getLong("Id_Funcionario"));
        i.setFechaHoraIngreso(rs.getTimestamp("Fecha_Hora_Ingreso").toLocalDateTime());
        i.setPuertaIngreso(rs.getString("Puerta_Ingreso"));
        return i;
    };

    public Ingreso findByIdQr(Long idQr) {
        String sql = "select * from Validacion where Id_QR = ?";
        List<Ingreso> resultado = jdbc.query(sql, rowMapper, idQr);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Long insert(Ingreso ingreso) {
        String sql = "insert into Validacion (Id_QR, Id_Dispositivo, Id_Funcionario, Puerta_Ingreso) " +
                     "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, ingreso.getIdQr());
            ps.setLong(2, ingreso.getIdDispositivo());
            ps.setLong(3, ingreso.getIdFuncionario());
            ps.setString(4, ingreso.getPuertaIngreso());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<Long> findSectoresValidadosPorFuncionario(Long idFuncionario, Long idEvento) {
        String sql = "select distinct es.Id_Evento_Sector " +
                     "from Validacion v " +
                     "join QR q on q.Id_QR = v.Id_QR " +
                     "join Entrada e on e.Id_Entrada = q.Id_Entrada " +
                     "join Evento_Sector es on es.Id_Evento_Sector = e.Id_Evento_Sector " +
                     "where v.Id_Funcionario = ? and es.Id_Evento = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("Id_Evento_Sector"), idFuncionario, idEvento);
    }
}