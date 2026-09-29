package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.model.Asset;
import com.nietproject.cryptoforge.model.PriceAlert;
import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.PriceAlertRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * =====================================================================
 * =====================================================================
 *
 * Demonstrates:
 * - Constructor Injection        — no @Autowired field injection
 * - @PostConstruct               — lifecycle: runs after DI completes
 * - @PreDestroy                  — lifecycle: runs before bean destroyed
 * - ExecutorService / Runnable             — manual thread management
 * - synchronized block                     — thread-safe price updates
 * - volatile boolean                       — visibility across threads
 *
 * NOTE: This class is defined as an explicit @Bean in AppConfig.java,
 * not just @Component auto-scan — this is the.
 *
 * NOTE: @Component is kept so Spring doesn't throw a "no bean" error
 * if AppConfig is absent, but the primary bean comes from AppConfig.
 */
public class MarketDataPoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MarketDataPoller.class);

    private static final String COINGECKO_URL =
        "https://api.coingecko.com/api/v3/simple/price" +
        "?ids=bitcoin,ethereum,solana,binancecoin,dogecoin,ripple,cardano,avalanche-2,matic-network,chainlink" +
        "&vs_currencies=usd" +
        "&include_24hr_change=true" +
        "&include_market_cap=true";

    private static final Map<String, String> COIN_ID_MAP = Map.of(
        "bitcoin",      "BTC",
        "ethereum",     "ETH",
        "solana",       "SOL",
        "binancecoin",  "BNB",
        "dogecoin",     "DOGE",
        "ripple",       "XRP",
        "cardano",      "ADA",
        "avalanche-2",  "AVAX",
        "matic-network","MATIC",
        "chainlink",    "LINK"
    );

    // Last known prices for the simulator fallback
    private static final Map<String, BigDecimal> lastKnownPrices = new ConcurrentHashMap<>();

    private final AssetRepository     assetRepository;
    private final RestTemplate         restTemplate;
    private final PriceAlertRepository alertRepository;

    private ExecutorService executor;
    private volatile boolean running = true;  // volatile — visible across threads
    private final Random     random  = new Random();

    /**
     * Constructor Injection —.
     * Spring (via AppConfig.marketDataPoller()) injects these dependencies.
     */
    public MarketDataPoller(AssetRepository assetRepository,
                            RestTemplate restTemplate,
                            PriceAlertRepository alertRepository) {
        this.assetRepository = assetRepository;
        this.restTemplate    = restTemplate;
        this.alertRepository = alertRepository;
    }

    /**
     * @PostConstruct — Bean Lifecycle.
     * Called by Spring AFTER all dependencies are injected.
     * Starts the background price polling thread.
     */
    @PostConstruct
    public void startPolling() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "market-data-poller");
            t.setDaemon(true); // daemon — doesn't block JVM shutdown
            return t;
        });
        executor.submit(this);
        log.info("MarketDataPoller bean initialized — background price polling started.");
    }

    /**
     * Main polling loop — runs in background thread.
     * Tries CoinGecko first; falls back to simulator if API fails.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Map<String, BigDecimal[]> prices = fetchLivePrices();
                if (prices.isEmpty()) {
                    prices = simulatePrices();
                    log.debug("Using simulated prices (CoinGecko unavailable)");
                } else {
                    log.debug("Fetched live prices from CoinGecko for {} assets", prices.size());
                }

                synchronized (this) {
                    updatePrices(prices);
                    checkAlerts();
                }

                Thread.sleep(15_000);   // poll every 15 seconds

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("MarketDataPoller thread interrupted — stopping.");
                break;
            } catch (Exception e) {
                log.warn("Price fetch error — will retry next tick: {}", e.getMessage());
                try { Thread.sleep(5_000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    /**
     * @PreDestroy — Bean Lifecycle.
     * Called by Spring BEFORE the bean is removed from the container (app shutdown).
     * Gracefully shuts down the executor and stops the polling thread.
     */
    @PreDestroy
    public void stopPolling() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("MarketDataPoller thread did not terminate cleanly.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("MarketDataPoller bean destroyed — polling thread stopped gracefully.");
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal[]> fetchLivePrices() {
        try {
            ResponseEntity<Map<String, Map<String, Object>>> response = restTemplate.exchange(
                COINGECKO_URL, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {}
            );

            Map<String, BigDecimal[]> result = new HashMap<>();
            if (response.getBody() == null) return result;

            for (Map.Entry<String, String> entry : COIN_ID_MAP.entrySet()) {
                String coinId = entry.getKey();
                String code   = entry.getValue();
                Map<String, Object> data = response.getBody().get(coinId);
                if (data != null && data.get("usd") != null) {
                    BigDecimal price     = new BigDecimal(data.get("usd").toString());
                    BigDecimal change24h = data.get("usd_24h_change") != null
                        ? new BigDecimal(data.get("usd_24h_change").toString()).setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    BigDecimal marketCap = data.get("usd_market_cap") != null
                        ? new BigDecimal(data.get("usd_market_cap").toString()).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    result.put(code, new BigDecimal[]{price, change24h, marketCap});
                    lastKnownPrices.put(code, price);
                }
            }
            return result;

        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * Simulated price movement — adds ±1% random noise to last known prices.
     * Used when CoinGecko is unreachable (rate-limited, offline, etc.).
     */
    private Map<String, BigDecimal[]> simulatePrices() {
        Map<String, BigDecimal> defaults = new HashMap<>();
        defaults.put("BTC",   new BigDecimal("67540"));
        defaults.put("ETH",   new BigDecimal("3521"));
        defaults.put("SOL",   new BigDecimal("178.50"));
        defaults.put("BNB",   new BigDecimal("608"));
        defaults.put("DOGE",  new BigDecimal("0.168"));
        defaults.put("XRP",   new BigDecimal("0.62"));
        defaults.put("ADA",   new BigDecimal("0.48"));
        defaults.put("AVAX",  new BigDecimal("38.50"));
        defaults.put("MATIC", new BigDecimal("0.92"));
        defaults.put("LINK",  new BigDecimal("18.20"));

        Map<String, BigDecimal[]> result = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : defaults.entrySet()) {
            String     code  = e.getKey();
            BigDecimal base  = lastKnownPrices.getOrDefault(code, e.getValue());
            double     noise = 1.0 + (random.nextDouble() * 0.02 - 0.01);
            BigDecimal price = base.multiply(BigDecimal.valueOf(noise)).setScale(8, RoundingMode.HALF_UP);
            BigDecimal change = BigDecimal.valueOf((random.nextDouble() * 4) - 2).setScale(4, RoundingMode.HALF_UP);
            result.put(code, new BigDecimal[]{price, change, BigDecimal.ZERO});
        }
        return result;
    }

    private void updatePrices(Map<String, BigDecimal[]> prices) {
        prices.forEach((code, data) -> {
            assetRepository.findById(code).ifPresent(asset -> {
                asset.setCurrentPriceUsd(data[0]);
                if (data[1] != null && data[1].compareTo(BigDecimal.ZERO) != 0)
                    asset.setPriceChange24h(data[1]);
                if (data[2] != null && data[2].compareTo(BigDecimal.ZERO) != 0)
                    asset.setMarketCapUsd(data[2]);
                assetRepository.save(asset);
            });
        });
    }

    /** Check all untriggered price alerts against current market prices. */
    private void checkAlerts() {
        try {
            alertRepository.findByTriggeredFalse().forEach(alert -> {
                assetRepository.findById(alert.getAssetCode()).ifPresent(asset -> {
                    BigDecimal current = asset.getCurrentPriceUsd();
                    BigDecimal target  = alert.getTargetPrice();
                    boolean fire = (alert.getDirection() == PriceAlert.Direction.ABOVE
                                        && current.compareTo(target) >= 0)
                                || (alert.getDirection() == PriceAlert.Direction.BELOW
                                        && current.compareTo(target) <= 0);
                    if (fire) {
                        alert.setTriggered(true);
                        alert.setTriggeredAt(java.time.LocalDateTime.now());
                        alertRepository.save(alert);
                        log.info("🔔 Price alert fired! {} {} ${}  (current: ${})",
                            alert.getAssetCode(), alert.getDirection(), target, current);
                    }
                });
            });
        } catch (Exception e) {
            log.warn("Alert check error: {}", e.getMessage());
        }
    }
}

