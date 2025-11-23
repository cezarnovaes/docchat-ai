package com.cezar.docchat.config;

import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {
    
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
    }
}