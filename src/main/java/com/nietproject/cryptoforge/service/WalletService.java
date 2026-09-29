package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    /**
     * Get all wallets for a user.
     */
    @Transactional(readOnly = true)
    public List<Wallet> getWallets(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    /**
     * Get a specific wallet balance.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId, String currencyCode) {
        return walletRepository.findByUserIdAndCurrencyCode(userId, currencyCode)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get USD balance.
     */
    @Transactional(readOnly = true)
    public BigDecimal getUsdBalance(Long userId) {
        return getBalance(userId, "USD");
    }

    /**
     * Get all non-zero wallets for portfolio display.
     */
    @Transactional(readOnly = true)
    public List<Wallet> getNonZeroWallets(Long userId) {
        return walletRepository.findNonZeroBalanceWallets(userId);
    }
}
