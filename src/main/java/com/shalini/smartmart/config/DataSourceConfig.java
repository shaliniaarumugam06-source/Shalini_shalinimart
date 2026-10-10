package com.shalini.smartmart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Ensure data directory exists for file-based H2 database
        if (datasourceUrl != null && datasourceUrl.contains(":file:")) {
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                if (created) {
                    logger.info("Created local data directory for persistent H2 database at: {}", dataDir.getAbsolutePath());
                }
            }
        }

        logger.info("Initializing DataSource for URL: {}", datasourceUrl);
        try {
            Class.forName(datasourceDriver);
            try (Connection conn = DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword)) {
                logger.info("Successfully established connection to primary database: {}", datasourceUrl);
                return DataSourceBuilder.create()
                        .driverClassName(datasourceDriver)
                        .url(datasourceUrl)
                        .username(datasourceUsername)
                        .password(datasourcePassword)
                        .build();
            }
        } catch (Exception e) {
            logger.warn("Primary database connection failed: {}. Falling back to H2 in-memory DB...", e.getMessage());
            return DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url("jdbc:h2:mem:smartmartdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                    .username("sa")
                    .password("")
                    .build();
        }
    }
}
