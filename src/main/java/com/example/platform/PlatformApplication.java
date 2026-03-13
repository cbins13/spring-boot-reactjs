package com.example.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.platform.modules")
@EntityScan(basePackages = "com.example.platform.modules")
public class PlatformApplication {
    public static void main(String[] args) {
        // Ensure Oracle JDBC treats timezone as offset, not region (required for Hibernate 6 with Oracle)
        if (System.getProperty("oracle.jdbc.timezoneAsRegion") == null) {
            System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        }
        SpringApplication.run(PlatformApplication.class, args);
    }
}

