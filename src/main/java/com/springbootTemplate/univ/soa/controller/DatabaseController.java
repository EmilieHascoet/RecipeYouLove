package com.springbootTemplate.univ.soa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/test")
    public Map<String, Object> testDatabaseConnections() {
        Map<String, Object> result = new HashMap<>();


        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            result.put("mysql", "✅ MySQL connection successful");
        } catch (Exception e) {
            result.put("mysql", "❌ MySQL connection failed: " + e.getMessage());
        }

        try {
            mongoTemplate.getCollection("test");
            result.put("mongodb", "✅ MongoDB connection successful");
        } catch (Exception e) {
            result.put("mongodb", "❌ MongoDB connection failed: " + e.getMessage());
        }

        return result;
    }
}
