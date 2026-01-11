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

        // If DATABASE_URL exists and doesn't start with jdbc:, convert it
        if (databaseUrl != null && !databaseUrl.startsWith("jdbc:")) {
            // Railway format: postgresql://user:pass@host:port/dbname
            // Convert to: jdbc:postgresql://user:pass@host:port/dbname
            databaseUrl = "jdbc:" + databaseUrl;
        }

        // If still null, use default for local development
        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/railway";
        }

        // DataSourceBuilder will automatically parse username and password from the JDBC URL
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(databaseUrl);

        return dataSourceBuilder.build();
    }
}
