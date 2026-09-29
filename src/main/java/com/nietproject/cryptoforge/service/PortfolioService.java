package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.model.Asset;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.TransactionRepository;
import com.nietproject.cryptoforge.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PortfolioService {

    private final WalletRepository      walletRepository;
    private final AssetRepository       assetRepository;
    private final TransactionRepository transactionRepository;
    private final OrderService          orderService;

    public PortfolioService(WalletRepository walletRepository,
                            AssetRepository assetRepository,
                            TransactionRepository transactionRepository,
                            OrderService orderService) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.transactionRepository = transactionRepository;
        this.orderService = orderService;
    }

    /**
     * Calculate full portfolio value using the stored procedure (.
     * Also returns per-asset breakdown.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPortfolio(Long userId) {
        BigDecimal cryptoValue = orderService.getPortfolioValueViaProcedure(userId);

        // JPA: get USD balance 
        BigDecimal usdBalance = walletRepository
                .findByUserIdAndCurrencyCode(userId, "USD")
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalValue = usdBalance.add(cryptoValue);

        // Per-asset holdings
        List<Map<String, Object>> holdings = new ArrayList<>();
        List<Wallet> cryptoWallets = walletRepository.findNonZeroBalanceWallets(userId)
                .stream()
                .filter(w -> !"USD".equals(w.getCurrencyCode()))
                .toList();

        for (Wallet wallet : cryptoWallets) {
            assetRepository.findById(wallet.getCurrencyCode()).ifPresent(asset -> {
                BigDecimal value = wallet.getBalance().multiply(asset.getCurrentPriceUsd())
                        .setScale(2, RoundingMode.HALF_UP);
                Map<String, Object> holding = new LinkedHashMap<>();
                holding.put("code",          asset.getCode());
                holding.put("name",          asset.getName());
                holding.put("quantity",      wallet.getBalance());
                holding.put("currentPrice",  asset.getCurrentPriceUsd());
                holding.put("valueUsd",      value);
                holding.put("priceChange24h",asset.getPriceChange24h());
                holdings.add(holding);
            });
        }

        // Stats from TransactionRepository
        BigDecimal totalInvested = transactionRepository.getTotalInvested(userId);
        BigDecimal totalRealized = transactionRepository.getTotalRealized(userId);
        if (totalInvested == null) totalInvested = BigDecimal.ZERO;
        if (totalRealized == null) totalRealized = BigDecimal.ZERO;

        Map<String, Object> portfolio = new LinkedHashMap<>();
        portfolio.put("usdBalance",    usdBalance);
        portfolio.put("cryptoValue",   cryptoValue);
        portfolio.put("totalValue",    totalValue);
        portfolio.put("totalInvested", totalInvested);
        portfolio.put("totalRealized", totalRealized);
        portfolio.put("holdings",      holdings);

        return portfolio;
    }
}
