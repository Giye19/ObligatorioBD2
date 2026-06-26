package com.ucu.ticketing.repository;

import com.ucu.ticketing.model.Documento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class DocumentoRepository {

    private final JdbcTemplate jdbc;
    private final PaisRepository paisRepository;

    public DocumentoRepository(JdbcTemplate jdbc, PaisRepository paisRepository) {
        this.jdbc = jdbc;
        this.paisRepository = paisRepository;
    }

    private final RowMapper<Documento> rowMapper = (rs, rowNum) -> {
        Documento d = new Documento();
        d.setIdDocumento(rs.getLong("Id_Documento"));
        d.setIdPais(rs.getLong("Id_Pais"));
        d.setTipoDocumento(rs.getString("Tipo_Documento"));
        d.setNumeroDocumento(rs.getString("Numero_Documento"));
        return d;
    };

    public Documento findById(Long idDocumento) {
        String sql = "select * from Documento where Id_Documento = ?";
        List<Documento> resultado = jdbc.query(sql, rowMapper, idDocumento);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean existsByDocumento(String nombrePais, String tipoDocumento, String numeroDocumento) {
        String sql = "select count(*) from Documento d " +
                     "join Pais p on p.Id_Pais = d.Id_Pais " +
                     "where p.Nombre_Pais = ? and d.Tipo_Documento = ? and d.Numero_Documento = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, nombrePais, tipoDocumento, numeroDocumento);
        return count != null && count > 0;
    }

    public Long insert(String nombrePais, String tipoDocumento, String numeroDocumento) {
        Long idPais = paisRepository.insertSiNoExiste(nombrePais);

        String sql = "insert into Documento (Id_Pais, Tipo_Documento, Numero_Documento) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idPais);
            ps.setString(2, tipoDocumento);
            ps.setString(3, numeroDocumento);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}