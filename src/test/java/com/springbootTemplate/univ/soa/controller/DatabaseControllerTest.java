package com.springbootTemplate.univ.soa.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Connection connection;

    @InjectMocks
    private DatabaseController databaseController;

    @Test
    @DisplayName("testDatabaseConnections devrait retourner succès pour MySQL et MongoDB")
    void testDatabaseConnections_Success() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertNotNull(result);
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("mongodb"));
        assertTrue(result.get("mysql").toString().contains("successful"));
        assertTrue(result.get("mongodb").toString().contains("successful"));

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).close();
        verify(mongoTemplate, times(1)).getCollection("test");
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec de connexion MySQL")
    void testDatabaseConnections_MySQLFailure() throws SQLException {

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertNotNull(result);
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.get("mysql").toString().contains("failed"));
        assertTrue(result.get("mysql").toString().contains("Connection failed"));
        assertTrue(result.containsKey("mongodb"));

        verify(dataSource, times(1)).getConnection();
        verify(mongoTemplate, times(1)).getCollection("test");
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec de connexion MongoDB")
    void testDatabaseConnections_MongoDBFailure() throws SQLException {

        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenThrow(new RuntimeException("MongoDB not available"));


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertNotNull(result);
        assertTrue(result.containsKey("mongodb"));
        assertTrue(result.get("mongodb").toString().contains("failed"));
        assertTrue(result.get("mongodb").toString().contains("MongoDB not available"));
        assertTrue(result.containsKey("mysql"));

        verify(dataSource, times(1)).getConnection();
        verify(mongoTemplate, times(1)).getCollection("test");
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec des deux connexions")
    void testDatabaseConnections_BothFailures() throws SQLException {

        when(dataSource.getConnection()).thenThrow(new SQLException("MySQL error"));
        when(mongoTemplate.getCollection("test")).thenThrow(new RuntimeException("MongoDB error"));


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertNotNull(result);
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("mongodb"));
        assertTrue(result.get("mysql").toString().contains("failed"));
        assertTrue(result.get("mongodb").toString().contains("failed"));

        verify(dataSource, times(1)).getConnection();
        verify(mongoTemplate, times(1)).getCollection("test");
    }

    @Test
    @DisplayName("testDatabaseConnections devrait toujours retourner une Map non null")
    void testDatabaseConnections_AlwaysReturnsNonNullMap() throws SQLException {

        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        Map<String, Object> result = databaseController.testDatabaseConnections();

        assertNotNull(result, "Le résultat ne devrait jamais être null");
        assertEquals(2, result.size(), "La Map devrait contenir exactement 2 entrées");
    }

    @Test
    @DisplayName("testDatabaseConnections devrait fermer la connexion MySQL même en cas d'erreur MongoDB")
    void testDatabaseConnections_ClosesConnectionOnMongoDBError() throws SQLException {

        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenThrow(new RuntimeException("MongoDB error"));


        databaseController.testDatabaseConnections();


        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("testDatabaseConnections devrait contenir les clés 'mysql' et 'mongodb'")
    void testDatabaseConnections_ContainsRequiredKeys() throws SQLException {

        when(dataSource.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();
        when(mongoTemplate.getCollection("test")).thenReturn(null);


        Map<String, Object> result = databaseController.testDatabaseConnections();


        assertTrue(result.containsKey("mysql"), "La Map devrait contenir la clé 'mysql'");
        assertTrue(result.containsKey("mongodb"), "La Map devrait contenir la clé 'mongodb'");
    }
}

