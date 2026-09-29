package com.nietproject.cryptoforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CryptoForge Application Entry Point
 *
 * @SpringBootApplication composes three annotations:
 *   1. @Configuration       — marks this as a Java config class
 *   2. @EnableAutoConfiguration — triggers Spring Boot's auto-configuration
 *   3. @ComponentScan       — scans this package for beans
 *
 * @ServletComponentScan — allows @WebServlet, @WebFilter, @WebListener
 *   annotations to register raw Servlet-API components (feature
 */
@SpringBootApplication
@ServletComponentScan   
@EnableScheduling       // Enables @Scheduled for periodic tasks
public class CryptoForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoForgeApplication.class, args);
    }
}
