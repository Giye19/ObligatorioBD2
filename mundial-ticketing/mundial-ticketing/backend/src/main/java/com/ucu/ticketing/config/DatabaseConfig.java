package com.ucu.ticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuración de acceso a datos.
 *
 * Spring Boot autoconfigura el DataSource con los valores de
 * application.properties. Acá solo exponemos el JdbcTemplate
 * como bean para inyectarlo en los repositories.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
