package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Ingreso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    /**
     * busca el registro de validacion asociado a un qr, si existe.
     * util para confirmar que una validacion efectivamente quedo
     * registrada despues de llamar a sp_validar_qr
     */
    public Ingreso findByIdQr(Long idQr) {
        String sql = "select * from Validacion where Id_QR = ?";
        List<Ingreso> resultado = jdbc.query(sql, rowMapper, idQr);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /**
     * lista los sectores (evento_sector) donde un funcionario ya
     * validó entradas dentro de un evento especifico. se usa para 
     * comparar contra sus sectores asignados
     */
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