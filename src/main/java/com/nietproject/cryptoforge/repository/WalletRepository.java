package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 *
 * Demonstrates BOTH derived query methods AND explicit @Query (JPQL).
 * Showing both styles is directly required by the specification ("Querying Entities").
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Derived query — finds all wallets for a user
    List<Wallet> findByUserId(Long userId);

    // Derived query — finds a specific wallet (e.g., user's USD wallet)
    Optional<Wallet> findByUserIdAndCurrencyCode(Long userId, String currencyCode);

    // Explicit JPQL @Query
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.balance > 0")
    List<Wallet> findNonZeroBalanceWallets(@Param("userId") Long userId);

    // Explicit JPQL @Query — calculate total USD value (used by portfolio)
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.user.id = :userId AND w.currencyCode = 'USD'")
    BigDecimal getTotalUsdBalance(@Param("userId") Long userId);

    // Modifying JPQL — update balance directly (used in non-critical paths)
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = :balance WHERE w.user.id = :userId AND w.currencyCode = :code")
    int updateBalance(@Param("userId") Long userId,
                      @Param("code") String currencyCode,
                      @Param("balance") BigDecimal balance);
}
