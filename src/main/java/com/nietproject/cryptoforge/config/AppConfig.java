package com.nietproject.cryptoforge.config;

import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.PriceAlertRepository;
import com.nietproject.cryptoforge.service.MarketDataPoller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * =====================================================================
 * =====================================================================
 *
 * Demonstrates:
 * - @Configuration                     — Java-based bean configuration class
 * - @Bean                              — Factory Method pattern (Spring's @Bean = factory method)
 * - Constructor Injection              — beans are injected via constructor parameters
 * - Explicit bean definition           — overrides component-scan auto-wiring for MarketDataPoller
 *
 * Why explicit here instead of relying on component scan alone?
 * Because the specification explicitly lists "Java/XML Config", "Bean Lifecycle",
 * and "Constructor Injection" — so we need a @Configuration class with
 * manual @Bean methods to point to in the viva.
 */
@Configuration  
public class AppConfig {

    /**
     * MarketDataPoller Bean — explicit factory method.
     *
     * Spring sees this @Bean method and calls it once at startup,
     * injecting the required dependencies via constructor injection.
     * The returned object is managed by the Spring IoC container.
     *
     * This is the Factory Method pattern as Spring implements it.
     */
    @Bean
    public MarketDataPoller marketDataPoller(AssetRepository assetRepository,
                                             RestTemplate restTemplate,
                                             PriceAlertRepository alertRepository) {
        return new MarketDataPoller(assetRepository, restTemplate, alertRepository);
    }

    /**
     * RestTemplate Bean — used by MarketDataPoller to call CoinGecko API.
     * Defined here (not component-scan) to keep HTTP client config centralized.
     */
    @Bean
    public RestTemplate restTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
