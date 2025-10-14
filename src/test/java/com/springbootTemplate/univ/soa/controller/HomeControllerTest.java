package com.springbootTemplate.univ.soa.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private HomeController homeController;

    @BeforeEach
    void setUp() {
        homeController = new HomeController();
    }

    @Test
    @DisplayName("home() devrait retourner le message de bienvenue")
    void testHome() {
        String result = homeController.home();

        assertNotNull(result);
        assertTrue(result.contains("RecipeYouLove API"));
        assertTrue(result.contains("running successfully"));
    }

    @Test
    @DisplayName("health() devrait retourner le statut de santé")
    void testHealth() {
        String result = homeController.health();

        assertNotNull(result);
        assertTrue(result.contains("Application is healthy"));
    }

    @Test
    @DisplayName("status() devrait retourner un objet StatusResponse non null")
    void testStatus() {
        Object result = homeController.status();

        assertNotNull(result);
        assertTrue(result instanceof HomeController.StatusResponse);
    }

    @Test
    @DisplayName("StatusResponse devrait avoir les bonnes valeurs")
    void testStatusResponse() {
        Object result = homeController.status();
        HomeController.StatusResponse statusResponse = (HomeController.StatusResponse) result;

        assertEquals("RecipeYouLove API", statusResponse.applicationName);
        assertEquals("1.0.0", statusResponse.version);
        assertEquals("Running", statusResponse.status);
    }

    @Test
    @DisplayName("Tous les endpoints devraient retourner des valeurs non vides")
    void testEndpointsReturnNonEmpty() {
        assertFalse(homeController.home().isEmpty());
        assertFalse(homeController.health().isEmpty());
        assertNotNull(homeController.status());
    }

    @Test
    @DisplayName("StatusResponse devrait pouvoir être créé avec un constructeur")
    void testStatusResponseConstructor() {
        HomeController.StatusResponse response = new HomeController.StatusResponse("Test", "2.0", "OK");

        assertEquals("Test", response.applicationName);
        assertEquals("2.0", response.version);
        assertEquals("OK", response.status);
    }

    @Test
    @DisplayName("home() devrait contenir un emoji")
    void testHomeContainsEmoji() {
        String result = homeController.home();
        assertTrue(result.contains("🚀"));
    }
}
