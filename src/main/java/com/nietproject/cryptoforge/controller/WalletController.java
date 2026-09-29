package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /** GET /api/wallet?userId=1 — all wallets for a user */
    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(@RequestParam Long userId) {
        return ResponseEntity.ok(walletService.getWallets(userId));
    }

    /** GET /api/wallet/balance?userId=1&currency=USD */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "USD") String currency) {
        BigDecimal balance = walletService.getBalance(userId, currency);
        return ResponseEntity.ok(Map.of(
            "userId",   userId,
            "currency", currency,
            "balance",  balance
        ));
    }
}
