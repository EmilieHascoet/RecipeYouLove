package com.springbootTemplate.univ.soa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "🚀 RecipeYouLove API is running successfully!";
    }

    @GetMapping("/health")
    public String health() {
        return "✅ Application is healthy and ready to serve requests";
    }

    @GetMapping("/api/status")
    public Object status() {
        return new StatusResponse("RecipeYouLove API", "1.0.0", "Running");
    }

    static class StatusResponse {
        public String applicationName;
        public String version;
        public String status;

        public StatusResponse(String applicationName, String version, String status) {
            this.applicationName = applicationName;
            this.version = version;
            this.status = status;
        }
    }
}
