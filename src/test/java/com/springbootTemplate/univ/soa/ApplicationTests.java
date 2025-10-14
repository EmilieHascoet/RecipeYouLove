package com.springbootTemplate.univ.soa;

import com.springbootTemplate.univ.soa.controller.DatabaseController;
import com.springbootTemplate.univ.soa.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour l'application RecipeYouLove
 * Vérifie que le contexte Spring Boot se charge correctement
 */
@SpringBootTest
class ApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Le contexte Spring devrait se charger correctement")
    void contextLoads() {
        assertNotNull(applicationContext, "Le contexte de l'application ne devrait pas être null");
    }

    @Test
    @DisplayName("HomeController devrait être présent dans le contexte")
    void homeControllerShouldBeLoaded() {
        HomeController homeController = applicationContext.getBean(HomeController.class);
        assertNotNull(homeController, "HomeController devrait être chargé dans le contexte Spring");
    }

    @Test
    @DisplayName("DatabaseController devrait être présent dans le contexte")
    void databaseControllerShouldBeLoaded() {
        DatabaseController databaseController = applicationContext.getBean(DatabaseController.class);
        assertNotNull(databaseController, "DatabaseController devrait être chargé dans le contexte Spring");
    }

    @Test
    @DisplayName("L'application devrait avoir tous les beans nécessaires")
    void applicationShouldHaveRequiredBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertTrue(beanNames.length > 0, "Le contexte devrait contenir au moins un bean");
    }

    @Test
    @DisplayName("Le nom de l'application devrait être configuré")
    void applicationNameShouldBeConfigured() {
        String applicationName = applicationContext.getApplicationName();
        assertNotNull(applicationName, "Le nom de l'application ne devrait pas être null");
    }

    @Test
    @DisplayName("L'ID de l'application devrait être défini")
    void applicationIdShouldBeDefined() {
        String id = applicationContext.getId();
        assertNotNull(id, "L'ID de l'application ne devrait pas être null");
        assertFalse(id.isEmpty(), "L'ID de l'application ne devrait pas être vide");
    }

    @Test
    @DisplayName("L'environnement Spring devrait être configuré")
    void environmentShouldBeConfigured() {
        assertNotNull(applicationContext.getEnvironment(), "L'environnement Spring ne devrait pas être null");
    }

    @Test
    @DisplayName("Le contexte devrait contenir les controllers nécessaires")
    void contextShouldContainRequiredControllers() {
        assertTrue(applicationContext.containsBean("homeController"), "Le contexte devrait contenir homeController");
        assertTrue(applicationContext.containsBean("databaseController"), "Le contexte devrait contenir databaseController");
    }
}
