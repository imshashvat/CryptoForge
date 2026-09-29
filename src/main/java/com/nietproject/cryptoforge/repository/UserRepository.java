package com.nietproject.cryptoforge.repository;

import com.nietproject.cryptoforge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 * Spring Data JPA auto-generates SQL for derived query methods at startup.
 * No implementation needed — framework provides it.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Derived query
    Optional<User> findByUsername(String username);

    // Derived query
    Optional<User> findByEmail(String email);

    // Derived query
    boolean existsByUsername(String username);

    // Derived query
    boolean existsByEmail(String email);
}
