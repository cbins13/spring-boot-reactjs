package com.example.platform.config;

import com.example.platform.common.logging.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class for logging setup including request interceptors
 * and log directory initialization.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoggingConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Value("${logging.file.path:logs}")
    private String logPath;

    @Value("${spring.application.name:platform}")
    private String applicationName;

    /**
     * Initialize logging configuration after bean construction
     */
    @PostConstruct
    public void initializeLogging() {
        createLogDirectories();
        logConfigurationInfo();
    }

    /**
     * Register the request logging interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/health/**",
                        "/metrics/**",
                        "/favicon.ico",
                        "/error",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                );
        
        // Request logging interceptor registered
    }

    /**
     * Create necessary log directories
     */
    private void createLogDirectories() {
        try {
            Path logDir = Paths.get(logPath);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Create archived logs subdirectory
            Path archivedDir = logDir.resolve("archived");
            if (!Files.exists(archivedDir)) {
                Files.createDirectories(archivedDir);
            }

            // Verify write permissions
            File logFile = new File(logDir.toFile(), "test-write.tmp");
            if (logFile.createNewFile()) {
                logFile.delete();
            }

        } catch (IOException e) {
            log.error("Failed to create log directories: {}", logPath, e);
            // Don't throw exception - let the application start but log the error
        }
    }

    /**
     * Log configuration information on application startup (errors only)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Application startup logging removed to save space
    }

    /**
     * Log basic configuration information (disabled to save space)
     */
    private void logConfigurationInfo() {
        // Configuration logging removed to save space
    }

    /**
     * Shutdown hook (disabled to save space)
     */
    public void onShutdown() {
        // Shutdown logging removed to save space
    }
}