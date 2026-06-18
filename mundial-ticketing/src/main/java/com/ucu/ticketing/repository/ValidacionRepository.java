package com.ucu.ticketing.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ValidacionRepository {

    private final JdbcTemplate jdbc;

    public ValidacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Queries SQL se implementan en pasos siguientes
}
