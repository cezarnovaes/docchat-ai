package com.cezar.docchat.config;

import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {
    
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().load();
        
        // Carrega JWT_SECRET
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION", "86400000"));
        
        // Carrega OPENAI_API_KEY
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
    }
}