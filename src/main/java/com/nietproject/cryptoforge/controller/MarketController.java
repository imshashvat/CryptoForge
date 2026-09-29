package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.model.Asset;
import com.nietproject.cryptoforge.model.Transaction;
import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.TransactionRepository;
import com.nietproject.cryptoforge.repository.UserRepository;
import com.nietproject.cryptoforge.repository.WalletRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class MarketController {

    private final AssetRepository       assetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;
    private final WalletRepository      walletRepository;

    public MarketController(AssetRepository assetRepository,
                            TransactionRepository transactionRepository,
                            UserRepository userRepository,
                            WalletRepository walletRepository) {
        this.assetRepository       = assetRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository        = userRepository;
        this.walletRepository      = walletRepository;
    }

    /** GET /api/assets — all assets sorted by market cap */
    @GetMapping("/assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetRepository.findAllByOrderByMarketCapUsdDesc());
    }

    /** GET /api/assets/{code} — single asset */
    @GetMapping("/assets/{code}")
    public ResponseEntity<Asset> getAsset(@PathVariable String code) {
        return assetRepository.findById(code.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/prices — slim live price feed for frontend polling.
     * Returns: [{code, name, price, change24h, marketCap}]
     */
    @GetMapping("/prices")
    public ResponseEntity<List<Map<String, Object>>> getPrices() {
        List<Map<String, Object>> prices = assetRepository.findAllByOrderByMarketCapUsdDesc()
            .stream()
            .map(a -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("code",       a.getCode());
                m.put("name",       a.getName());
                m.put("price",      a.getCurrentPriceUsd());
                m.put("change24h",  a.getPriceChange24h());
                m.put("marketCap",  a.getMarketCapUsd());
                return m;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }

    /** GET /api/transactions?userId=1 — full ledger for a user */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam Long userId) {
        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /**
     * GET /api/leaderboard — top traders ranked by total wallet value.
     * Shows masked usernames (first 3 chars + ***)
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<User> users = userRepository.findAll().stream()
            .filter(User::isActive)
            .toList();

        List<Map<String, Object>> board = users.stream().map(u -> {
            // Sum all wallet balances (USD + crypto wallets balance field)
            BigDecimal total = walletRepository.findByUserId(u.getId())
                .stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            String masked = u.getUsername().length() > 3
                ? u.getUsername().substring(0, 3) + "***"
                : u.getUsername();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username",   masked);
            row.put("totalValue", total);
            row.put("role",       u.getRole().name());
            return row;
        })
        .sorted((a, b) -> ((BigDecimal) b.get("totalValue"))
            .compareTo((BigDecimal) a.get("totalValue")))
        .collect(Collectors.toList());

        // Add rank
        for (int i = 0; i < board.size(); i++) {
            ((Map<String, Object>) board.get(i)).put("rank", i + 1);
        }

        return ResponseEntity.ok(board);
    }

    /**
     * GET /api/market/recent-trades — last 20 platform trades (anonymized).
     * Used for the live trade feed on the order form.
     */
    @GetMapping("/market/recent-trades")
    public ResponseEntity<List<Map<String, Object>>> getRecentTrades() {
        List<Map<String, Object>> trades = transactionRepository.findTop50ByOrderByCreatedAtDesc()
            .stream()
            .limit(20)
            .map(t -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("assetCode",  t.getAsset() != null ? t.getAsset().getCode() : "?");
                m.put("type",       t.getType().name());
                m.put("quantity",   t.getQuantity());
                m.put("priceUsd",   t.getPriceUsd());
                m.put("totalValue", t.getTotalValueUsd());
                m.put("time",       t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
                return m;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(trades);
    }

    /**
     * GET /api/market/candles/{code} — 72 hourly OHLC candles for TradingView chart.
     * Generated via seeded random walk anchored to current live price.
     */
    @GetMapping("/market/candles/{code}")
    public ResponseEntity<List<Map<String, Object>>> getCandles(@PathVariable String code) {
        Optional<Asset> assetOpt = assetRepository.findById(code.toUpperCase());
        if (assetOpt.isEmpty()) return ResponseEntity.notFound().build();

        double current = assetOpt.get().getCurrentPriceUsd().doubleValue();
        List<Map<String, Object>> candles = new ArrayList<>();

        long nowEpoch = System.currentTimeMillis() / 1000L;
        // Seed by code so the same asset always gets the same "history shape"
        Random rng = new Random((long) code.hashCode() * 31 + (nowEpoch / 3600));

        // Start 72 hours ago at price ± 4 %
        double price = current * (0.96 + rng.nextDouble() * 0.08);
        double vol   = current * 0.012; // ~1.2% hourly volatility

        for (int i = 72; i >= 1; i--) {
            long time = nowEpoch - (long) i * 3600;

            double open  = price;
            double move  = rng.nextGaussian() * vol;
            double close = Math.max(open * 0.85, open + move);
            double hi    = Math.max(open, close) + Math.abs(rng.nextGaussian() * vol * 0.5);
            double lo    = Math.min(open, close) - Math.abs(rng.nextGaussian() * vol * 0.5);

            // Final candle snaps to live price
            if (i == 1) { close = current; hi = Math.max(hi, current); lo = Math.min(lo, current); }

            Map<String, Object> c = new LinkedHashMap<>();
            c.put("time",  time);
            c.put("open",  round2(open));
            c.put("high",  round2(hi));
            c.put("low",   round2(lo));
            c.put("close", round2(close));
            candles.add(c);
            price = close;
        }
        return ResponseEntity.ok(candles);
    }

    /** GET /api/market/sparkline/{code} — 24 hourly close prices for sparkline mini-charts */
    @GetMapping("/market/sparkline/{code}")
    public ResponseEntity<List<Double>> getSparkline(@PathVariable String code) {
        Optional<Asset> assetOpt = assetRepository.findById(code.toUpperCase());
        if (assetOpt.isEmpty()) return ResponseEntity.notFound().build();

        double current = assetOpt.get().getCurrentPriceUsd().doubleValue();
        Random rng = new Random((long) code.hashCode() * 17);
        List<Double> points = new ArrayList<>();
        double p = current * (0.97 + rng.nextDouble() * 0.06);
        double v = current * 0.008;
        for (int i = 0; i < 24; i++) {
            p = Math.max(p * 0.8, p + rng.nextGaussian() * v);
            points.add(round2(p));
        }
        points.set(points.size() - 1, round2(current));
        return ResponseEntity.ok(points);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
