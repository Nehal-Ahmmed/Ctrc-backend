package com.ctrc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CtrcBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CtrcBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("SELECT 1");
                System.out.println("=================================================");
                System.out.println("✅ DATABASE CONNECTION SUCCESSFUL! Connected to ctrcdb.");
                System.out.println("=================================================");
            } catch (Exception e) {
                System.out.println("=================================================");
                System.out.println("❌ DATABASE CONNECTION FAILED!");
                System.out.println(e.getMessage());
                System.out.println("=================================================");
            }
        };
    }
}
