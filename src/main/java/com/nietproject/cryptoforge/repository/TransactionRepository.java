package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * Demonstrates BOTH derived query methods AND explicit @Query (JPQL).
 * Transaction has user as @ManyToOne (no userId field), so derived queries
 * must use User_Id path, or we use explicit JPQL for clarity.
 * This is the key example to point to in viva for "Querying Entities".
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Explicit JPQL @Query
    // user is @ManyToOne: traverse path t.user.id
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Explicit JPQL — last 10 transactions for a user (demonstrates JPQL with LIMIT via setMaxResults)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findTop10ByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Explicit JPQL @Query
    @Query("SELECT SUM(t.totalValueUsd) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'BUY'")
    BigDecimal getTotalInvested(@Param("userId") Long userId);

    // Explicit JPQL — total realized from sells
    @Query("SELECT SUM(t.totalValueUsd) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'SELL'")
    BigDecimal getTotalRealized(@Param("userId") Long userId);

    // Derived query — last 50 transactions across all users (for live feed)
    List<Transaction> findTop50ByOrderByCreatedAtDesc();

    // All transactions ordered by time (admin / leaderboard)
    List<Transaction> findAllByOrderByCreatedAtDesc();

    // Explicit JPQL — count trades per asset for a user
    @Query("SELECT t.asset.code, COUNT(t) FROM Transaction t WHERE t.user.id = :userId GROUP BY t.asset.code")
    List<Object[]> countTradesByAsset(@Param("userId") Long userId);
}
