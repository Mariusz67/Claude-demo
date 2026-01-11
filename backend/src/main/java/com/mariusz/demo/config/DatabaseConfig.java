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
            // Convert to: jdbc:postgresql://host:port/dbname
            databaseUrl = "jdbc:" + databaseUrl;
        }

        // If still null, use default for local development
        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/railway";
        }

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(databaseUrl);

        // Set username and password from environment if DATABASE_URL doesn't contain them
        String username = System.getenv("DATABASE_USERNAME");
        String password = System.getenv("DATABASE_PASSWORD");

        if (username != null) {
            dataSourceBuilder.username(username);
        }
        if (password != null) {
            dataSourceBuilder.password(password);
        }

        return dataSourceBuilder.build();
    }
}
