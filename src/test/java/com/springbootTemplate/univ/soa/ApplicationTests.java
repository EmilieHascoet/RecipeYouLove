package com.springbootTemplate.univ.soa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTests {

    @Test
    @DisplayName("L'application devrait avoir une méthode main")
    void applicationShouldHaveMainMethod() {
        assertDoesNotThrow(() -> {
            Application.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    @DisplayName("La classe Application devrait être publique")
    void applicationShouldBePublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(Application.class.getModifiers()));
    }

    @Test
    @DisplayName("L'application devrait avoir l'annotation SpringBootApplication")
    void applicationShouldHaveSpringBootAnnotation() {
        assertNotNull(Application.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
