package com.mariusz.demo.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        if (databaseUrl != null && !databaseUrl.startsWith("jdbc:")) {
            // Railway format: postgresql://user:pass@host:port/dbname
            // Need to extract credentials and build proper JDBC URL

            // Remove postgresql:// prefix
            String urlWithoutProtocol = databaseUrl.substring("postgresql://".length());

            // Extract username and password (before @)
            int atIndex = urlWithoutProtocol.indexOf('@');
            String credentials = urlWithoutProtocol.substring(0, atIndex);
            String hostAndDb = urlWithoutProtocol.substring(atIndex + 1);

            // Split credentials
            int colonIndex = credentials.indexOf(':');
            String username = credentials.substring(0, colonIndex);
            String password = credentials.substring(colonIndex + 1);

            // Build JDBC URL without credentials
            String jdbcUrl = "jdbc:postgresql://" + hostAndDb;

            dataSourceBuilder.url(jdbcUrl);
            dataSourceBuilder.username(username);
            dataSourceBuilder.password(password);
        } else if (databaseUrl != null) {
            // Already in JDBC format
            dataSourceBuilder.url(databaseUrl);
        } else {
            // Local development fallback
            dataSourceBuilder.url("jdbc:postgresql://localhost:5432/railway");
        }

        return dataSourceBuilder.build();
    }
}
