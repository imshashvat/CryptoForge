package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PriceAlert> findByTriggeredFalse();
    List<PriceAlert> findByUserIdAndTriggeredFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndTriggeredFalse(Long userId);
}
