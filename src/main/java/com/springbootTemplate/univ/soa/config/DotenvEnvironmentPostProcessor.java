package com.springbootTemplate.univ.soa.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger logger = Logger.getLogger(DotenvEnvironmentPostProcessor.class.getName());

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();


            Map<String, Object> envProperties = new HashMap<>();


            dotenv.entries().forEach(entry -> envProperties.put(entry.getKey(), entry.getValue()));


            MapPropertySource propertySource = new MapPropertySource("dotenv-properties", envProperties);
            environment.getPropertySources().addFirst(propertySource);

            logger.info("Fichier .env chargé avec succès - " + envProperties.size() + " variables trouvées");

        } catch (Exception e) {
            logger.severe("Erreur lors du chargement du fichier .env : " + e.getMessage());
        }
    }
}
