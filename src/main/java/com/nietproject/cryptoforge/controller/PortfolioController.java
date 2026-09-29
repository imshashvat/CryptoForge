package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * GET /api/portfolio?userId=1
     * Returns full portfolio: USD balance, crypto holdings, total value,
     * invested amount, realized P&L.
     * Internally calls the stored procedure (.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPortfolio(@RequestParam Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(userId));
    }
}
