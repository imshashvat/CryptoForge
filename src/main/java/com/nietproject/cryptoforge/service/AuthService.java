package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.dto.AuthResponse;
import com.nietproject.cryptoforge.dto.LoginRequest;
import com.nietproject.cryptoforge.dto.RegisterRequest;
import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.UserRepository;
import com.nietproject.cryptoforge.repository.WalletRepository;
import com.nietproject.cryptoforge.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository        userRepository;
    private final WalletRepository      walletRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtils              jwtUtils;
    private final AuthenticationManager authenticationManager;

    private static final List<String> SUPPORTED_ASSETS = List.of("BTC", "ETH", "SOL", "BNB", "DOGE");
    private static final BigDecimal   STARTER_USD      = new BigDecimal("10000.00");

    public AuthService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
                       AuthenticationManager authenticationManager) {
        this.userRepository      = userRepository;
        this.walletRepository    = walletRepository;
        this.passwordEncoder     = passwordEncoder;
        this.jwtUtils            = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.USER)
                .active(true)
                .build();
        user = userRepository.save(user);

        Wallet usdWallet = Wallet.builder()
                .user(user)
                .currencyCode("USD")
                .balance(STARTER_USD)
                .build();
        walletRepository.save(usdWallet);

        final User savedUser = user;
        SUPPORTED_ASSETS.forEach(code -> {
            Wallet w = Wallet.builder()
                    .user(savedUser)
                    .currencyCode(code)
                    .balance(BigDecimal.ZERO)
                    .build();
            walletRepository.save(w);
        });

        String token = jwtUtils.generateToken(user.getUsername());
        log.info("New user registered: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .usdBalance(STARTER_USD)
                .message("Registration successful! You start with $10,000 USD.")
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        User user = (User) auth.getPrincipal();
        String token = jwtUtils.generateToken(user.getUsername());

        BigDecimal usdBalance = walletRepository
                .findByUserIdAndCurrencyCode(user.getId(), "USD")
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        log.info("User logged in: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .usdBalance(usdBalance)
                .message("Login successful")
                .build();
    }
}
