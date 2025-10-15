package com.springbootTemplate.univ.soa.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class DatabaseControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Connection connection;

    private DatabaseController databaseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseController = new DatabaseController();
        setField(databaseController, "dataSource", dataSource);
        setField(databaseController, "mongoTemplate", mongoTemplate);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("testDatabaseConnections devrait retourner succès pour MySQL et MongoDB")
    void testDatabaseConnections_Success() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenReturn(null);



        Map<String, Object> result = databaseController.testDatabaseConnections();



        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get("mysql").toString().contains("successful"));
        assertTrue(result.get("mongodb").toString().contains("successful"));
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec MySQL")
    void testDatabaseConnections_MySQLFailure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        // Act
        Map<String, Object> result = databaseController.testDatabaseConnections();


        // Assert
        assertTrue(result.get("mysql").toString().contains("failed"));
        assertTrue(result.get("mongodb").toString().contains("successful"));
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec MongoDB")
    void testDatabaseConnections_MongoDBFailure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        when(mongoTemplate.getCollection("test")).thenThrow(new RuntimeException("MongoDB error"));


        // Act
        Map<String, Object> result = databaseController.testDatabaseConnections();


        // Assert
        assertTrue(result.get("mysql").toString().contains("successful"));
        assertTrue(result.get("mongodb").toString().contains("failed"));
    }

    @Test
    @DisplayName("testDatabaseConnections devrait fermer la connexion MySQL")
    void testDatabaseConnections_ClosesConnection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        databaseController.testDatabaseConnections();


        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("La Map de résultat devrait toujours contenir mysql et mongodb")
    void testDatabaseConnections_AlwaysReturnsRequiredKeys() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("mongodb"));
    }
}
