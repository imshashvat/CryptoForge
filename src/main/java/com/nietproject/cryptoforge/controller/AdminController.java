package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AdminController — admin dashboard with extended rights.
 * Only accessible to ROLE_ADMIN sessions.
 *
 * Admin Powers:
 *  - View all users, orders, assets, global USD pool
 *  - Ban / Unban users
 *  - Adjust user USD balance (credit / debit)
 *  - Reset a user's entire portfolio (wipe crypto wallets)
 *  - View all platform transactions
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository         userRepository;
    private final OrderRepository        orderRepository;
    private final AssetRepository        assetRepository;
    private final WalletRepository       walletRepository;
    private final TransactionRepository  transactionRepository;

    public AdminController(UserRepository userRepository,
                           OrderRepository orderRepository,
                           AssetRepository assetRepository,
                           WalletRepository walletRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository        = userRepository;
        this.orderRepository       = orderRepository;
        this.assetRepository       = assetRepository;
        this.walletRepository      = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    // ─── Guard helper ────────────────────────────────────────────────────────
    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"))
            && session.getAttribute("userId") != null;
    }

    // ─── Main dashboard ──────────────────────────────────────────────────────
    @GetMapping({"", "/"})
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<User> allUsers  = userRepository.findAll();
        long totalOrders     = orderRepository.count();
        long totalUsers      = allUsers.size();
        long activeUsers     = allUsers.stream().filter(User::isActive).count();

        BigDecimal totalUsd = walletRepository.findAll().stream()
            .filter(w -> "USD".equals(w.getCurrencyCode()))
            .map(Wallet::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("adminUsername",    session.getAttribute("username"));
        model.addAttribute("allUsers",         allUsers);
        model.addAttribute("totalUsers",       totalUsers);
        model.addAttribute("activeUsers",      activeUsers);
        model.addAttribute("bannedUsers",      totalUsers - activeUsers);
        model.addAttribute("totalOrders",      totalOrders);
        model.addAttribute("totalUsd",         totalUsd);
        model.addAttribute("assets",           assetRepository.findAllByOrderByMarketCapUsdDesc());
        model.addAttribute("recentTxns",       transactionRepository.findTop50ByOrderByCreatedAtDesc());

        return "admin";
    }

    // ─── Ban a user ──────────────────────────────────────────────────────────
    @PostMapping("/ban/{userId}")
    public String banUser(@PathVariable Long userId,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        Long adminId = (Long) session.getAttribute("userId");
        if (userId.equals(adminId)) {
            ra.addFlashAttribute("adminError", "You cannot ban yourself.");
            return "redirect:/admin";
        }
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
        ra.addFlashAttribute("adminSuccess", "User #" + userId + " has been banned.");
        return "redirect:/admin";
    }

    // ─── Unban a user ────────────────────────────────────────────────────────
    @PostMapping("/unban/{userId}")
    public String unbanUser(@PathVariable Long userId,
                            HttpSession session,
                            RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(true);
            userRepository.save(u);
        });
        ra.addFlashAttribute("adminSuccess", "User #" + userId + " has been unbanned.");
        return "redirect:/admin";
    }

    // ─── Adjust user USD balance ──────────────────────────────────────────────
    @PostMapping("/balance/{userId}")
    public String adjustBalance(@PathVariable Long userId,
                                @RequestParam BigDecimal amount,
                                @RequestParam String action, // "credit" or "debit"
                                HttpSession session,
                                RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("adminError", "Amount must be positive.");
            return "redirect:/admin";
        }
        Optional<Wallet> walletOpt = walletRepository.findByUserIdAndCurrencyCode(userId, "USD");
        if (walletOpt.isEmpty()) {
            ra.addFlashAttribute("adminError", "USD wallet not found for user #" + userId);
            return "redirect:/admin";
        }
        Wallet wallet = walletOpt.get();
        if ("credit".equals(action)) {
            wallet.setBalance(wallet.getBalance().add(amount));
            ra.addFlashAttribute("adminSuccess",
                String.format("Credited $%.2f to user #%d.", amount, userId));
        } else {
            if (wallet.getBalance().compareTo(amount) < 0) {
                ra.addFlashAttribute("adminError", "Insufficient balance for debit.");
                return "redirect:/admin";
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
            ra.addFlashAttribute("adminSuccess",
                String.format("Debited $%.2f from user #%d.", amount, userId));
        }
        walletRepository.save(wallet);
        return "redirect:/admin";
    }

    // ─── Reset crypto portfolio (zero out all non-USD wallets) ───────────────
    @PostMapping("/reset/{userId}")
    public String resetPortfolio(@PathVariable Long userId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        List<Wallet> wallets = walletRepository.findAll().stream()
            .filter(w -> w.getUser() != null
                      && w.getUser().getId().equals(userId)
                      && !"USD".equals(w.getCurrencyCode()))
            .toList();
        wallets.forEach(w -> { w.setBalance(BigDecimal.ZERO); walletRepository.save(w); });
        ra.addFlashAttribute("adminSuccess",
            "Crypto portfolio reset for user #" + userId + ".");
        return "redirect:/admin";
    }

    // ─── Promote user to ADMIN ───────────────────────────────────────────────
    @PostMapping("/promote/{userId}")
    public String promoteUser(@PathVariable Long userId,
                              HttpSession session,
                              RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        userRepository.findById(userId).ifPresent(u -> {
            u.setRole(User.Role.ADMIN);
            userRepository.save(u);
        });
        ra.addFlashAttribute("adminSuccess", "User #" + userId + " promoted to ADMIN.");
        return "redirect:/admin";
    }

    // ─── Demote admin to USER ────────────────────────────────────────────────
    @PostMapping("/demote/{userId}")
    public String demoteUser(@PathVariable Long userId,
                             HttpSession session,
                             RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        Long adminId = (Long) session.getAttribute("userId");
        if (userId.equals(adminId)) {
            ra.addFlashAttribute("adminError", "You cannot demote yourself.");
            return "redirect:/admin";
        }
        userRepository.findById(userId).ifPresent(u -> {
            u.setRole(User.Role.USER);
            userRepository.save(u);
        });
        ra.addFlashAttribute("adminSuccess", "User #" + userId + " demoted to USER.");
        return "redirect:/admin";
    }

    // ─── API: leaderboard data ────────────────────────────────────────────────
    @GetMapping("/api/leaderboard")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> leaderboard(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).build();
        // Simple leaderboard: sum USD wallets per user
        List<Map<String, Object>> board = userRepository.findAll().stream()
            .filter(User::isActive)
            .map(u -> {
                BigDecimal total = walletRepository.findAll().stream()
                    .filter(w -> w.getUser() != null && w.getUser().getId().equals(u.getId()))
                    .map(Wallet::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return Map.<String, Object>of(
                    "username", u.getUsername(),
                    "total", total
                );
            })
            .sorted((a, b) -> ((BigDecimal)b.get("total")).compareTo((BigDecimal)a.get("total")))
            .toList();
        return ResponseEntity.ok(board);
    }
}
