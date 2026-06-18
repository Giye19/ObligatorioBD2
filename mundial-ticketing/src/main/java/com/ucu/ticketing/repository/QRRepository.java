package com.ucu.ticketing.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QRRepository {

    private final JdbcTemplate jdbc;

    public QRRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Queries SQL se implementan en pasos siguientes
}
