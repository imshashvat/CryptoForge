package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * Demonstrates BOTH derived query methods AND explicit @Query (JPQL).
 * Order has user as @ManyToOne, so we use explicit JPQL for user-id filtering
 * and derived methods for status-based queries via nested property path.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Explicit JPQL @Query
    // User is a @ManyToOne relationship; use JPQL path o.user.id to traverse it
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Explicit JPQL @Query — orders filtered by status
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") Order.OrderStatus status);

    // Explicit JPQL — recent orders across all users (admin use)
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders();

    // Explicit JPQL — count completed orders for dashboard stats
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    long countCompletedOrders(@Param("userId") Long userId);
}
