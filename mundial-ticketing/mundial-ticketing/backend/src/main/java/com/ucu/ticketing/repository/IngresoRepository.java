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

// maneja el acceso a datos de la tabla Ingreso
// registra cada validacion exitosa en puerta
@Repository
public class IngresoRepository {

    private final JdbcTemplate jdbc;

    public IngresoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Ingreso> rowMapper = (rs, rowNum) -> {
        Ingreso i = new Ingreso();
        i.setIdIngreso(rs.getInt("id_ingreso"));
        i.setIdEntrada(rs.getInt("id_entrada"));
        i.setIdQr(rs.getInt("id_qr"));
        i.setIdDispositivo(rs.getString("id_dispositivo"));
        i.setHoraIngreso(rs.getTimestamp("hora_ingreso").toLocalDateTime());
        i.setPuertaIngreso(rs.getString("puerta_ingreso"));
        return i;
    };

    // busca un ingreso por id de entrada, devuelve null si esa
    // entrada todavia no fue validada en ninguna puerta
    public Ingreso findByEntrada(Integer idEntrada) {
        String sql = "select * from Ingreso where id_entrada = ?";
        List<Ingreso> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // inserta un nuevo registro de ingreso y devuelve el id autogenerado
    public Integer insert(Ingreso ingreso) {
        String sql = "insert into Ingreso (id_entrada, id_qr, id_dispositivo, puerta_ingreso) " +
                     "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, ingreso.getIdEntrada());
            ps.setInt(2, ingreso.getIdQr());
            ps.setString(3, ingreso.getIdDispositivo());
            ps.setString(4, ingreso.getPuertaIngreso());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // devuelve los sectores distintos donde un funcionario valido
    // entradas dentro de un evento especifico
    // se usa para comparar contra sus sectores asignados y verificar
    // si cumplio con la regla de validar en todos los que le tocaron
    public List<String> findSectoresValidadosPorFuncionario(String mailFuncionario, Integer idEvento) {
        String sql = "select distinct e.letra_sector " +
                     "from Ingreso i " +
                     "join Dispositivo d on d.id_dispositivo = i.id_dispositivo " +
                     "join Entrada e on e.id_entrada = i.id_entrada " +
                     "where d.mail_funcionario = ? and e.id_evento = ?";
        return jdbc.queryForList(sql, String.class, mailFuncionario, idEvento);
    }
}