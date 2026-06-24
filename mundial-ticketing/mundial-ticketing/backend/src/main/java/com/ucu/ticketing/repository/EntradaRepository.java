package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Entrada;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

// maneja el acceso a datos de la tabla Entrada
@Repository
public class EntradaRepository {

    private final JdbcTemplate jdbc;

    public EntradaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Entrada> rowMapper = (rs, rowNum) -> {
        Entrada e = new Entrada();
        e.setIdEntrada(rs.getInt("id_entrada"));
        e.setIdVenta(rs.getInt("id_venta"));
        e.setIdEvento(rs.getInt("id_evento"));
        e.setIdEstadio(rs.getInt("id_estadio"));
        e.setLetraSector(rs.getString("letra_sector"));
        e.setMailPropietario(rs.getString("mail_propietario"));
        e.setEstado(rs.getString("estado"));
        e.setCostoEntrada(rs.getBigDecimal("costo_entrada"));
        e.setCantTransferencias(rs.getInt("cant_transferencias"));
        return e;
    };

    // busca una entrada por id, devuelve null si no existe
    public Entrada findById(Integer idEntrada) {
        String sql = "select * from Entrada where id_entrada = ?";
        List<Entrada> resultado = jdbc.query(sql, rowMapper, idEntrada);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // devuelve todas las entradas generadas por una venta
    public List<Entrada> findByVenta(Integer idVenta) {
        String sql = "select * from Entrada where id_venta = ?";
        return jdbc.query(sql, rowMapper, idVenta);
    }

    // devuelve todas las entradas que actualmente tiene asignadas
    // un usuario (propietario actual), sin importar de que venta vinieron
    // se usa en "mis entradas"
    public List<Entrada> findByPropietario(String mailPropietario) {
        String sql = "select * from Entrada where mail_propietario = ? " +
                     "order by id_entrada desc";
        return jdbc.query(sql, rowMapper, mailPropietario);
    }

    // cuenta cuantas entradas activas (no canceladas) existen
    // para un evento+sector especifico, usado para calcular
    // disponibilidad: capacidad - cantidad vendida
    // se consideran "vendidas" las entradas en estado activa,
    // transferida o consumida (todo excepto que no haya llegado a existir)
    public int countEntradasVendidas(Integer idEvento, Integer idEstadio, String letraSector) {
        String sql = "select count(*) from Entrada " +
                     "where id_evento = ? and id_estadio = ? and letra_sector = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, idEvento, idEstadio, letraSector);
        return count != null ? count : 0;
    }

    // inserta una nueva entrada y devuelve el id autogenerado
    // estado inicial siempre ACTIVA, cant_transferencias siempre 0
    public Integer insert(Entrada entrada) {
        String sql = "insert into Entrada " +
                     "(id_venta, id_evento, id_estadio, letra_sector, mail_propietario, " +
                     " estado, costo_entrada, cant_transferencias) " +
                     "values (?, ?, ?, ?, ?, 'ACTIVA', ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, entrada.getIdVenta());
            ps.setInt(2, entrada.getIdEvento());
            ps.setInt(3, entrada.getIdEstadio());
            ps.setString(4, entrada.getLetraSector());
            ps.setString(5, entrada.getMailPropietario());
            ps.setBigDecimal(6, entrada.getCostoEntrada());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // cambia el propietario de una entrada (se llama al aceptar
    // una transferencia) e incrementa el contador de transferencias
    public void transferirPropietario(Integer idEntrada, String nuevoPropietario) {
        String sql = "update Entrada set mail_propietario = ?, " +
                     "estado = 'ACTIVA', cant_transferencias = cant_transferencias + 1 " +
                     "where id_entrada = ?";
        jdbc.update(sql, nuevoPropietario, idEntrada);
    }

    // marca una entrada como consumida de forma irreversible,
    // se llama al validarse el ingreso en puerta
    public void marcarConsumida(Integer idEntrada) {
        String sql = "update Entrada set estado = 'CONSUMIDA' where id_entrada = ?";
        jdbc.update(sql, idEntrada);
    }

    // devuelve el ranking de eventos con mas entradas vendidas,
    // limitado a una cantidad maxima
    // se usa para la funcionalidad de reportes/estadisticas
    public List<Object[]> findRankingEventos(int limite) {
        String sql = "select e.id_evento, count(*) as cantidad " +
                     "from Entrada e " +
                     "group by e.id_evento " +
                     "order by cantidad desc " +
                     "limit ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Object[]{rs.getInt("id_evento"), rs.getInt("cantidad")},
                limite);
    }
}