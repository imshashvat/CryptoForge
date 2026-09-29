package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    // Derived query — find assets ordered by market cap for the market page
    List<Asset> findAllByOrderByMarketCapUsdDesc();
}
