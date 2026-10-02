package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.UserRepository;
import com.nietproject.cryptoforge.repository.WalletRepository;
import com.nietproject.cryptoforge.service.AuthService;
import com.nietproject.cryptoforge.service.PortfolioService;
import com.nietproject.cryptoforge.service.WalletService;
import com.nietproject.cryptoforge.repository.OrderRepository;
import com.nietproject.cryptoforge.repository.TransactionRepository;
import com.nietproject.cryptoforge.repository.AssetRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.nietproject.cryptoforge.dto.OrderRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Spring MVC Controller 
 * Handles standard web browser flows, page rendering, session management,
 * and passes attributes to JSP pages .
 */
@Controller
public class WebViewController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebViewController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PortfolioService portfolioService;
    private final OrderRepository orderRepository;
    private final com.nietproject.cryptoforge.service.OrderService orderService;
    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final AuthService authService;

    public WebViewController(AuthenticationManager authenticationManager,
                             UserRepository userRepository,
                             WalletRepository walletRepository,
                             PortfolioService portfolioService,
                             OrderRepository orderRepository,
                             com.nietproject.cryptoforge.service.OrderService orderService,
                             TransactionRepository transactionRepository,
                             AssetRepository assetRepository,
                             AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.portfolioService = portfolioService;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.transactionRepository = transactionRepository;
        this.assetRepository = assetRepository;
        this.authService = authService;
    }


    @GetMapping("/markets")
    public String redirectMarkets() {
        return "redirect:/dashboard";
    }

    @GetMapping("/trade")
    public String redirectTrade() {
        return "redirect:/order";
    }

    @GetMapping("/wallet")
    public String redirectWallet() {
        return "redirect:/portfolio";
    }

    @GetMapping("/help")
    public String redirectHelp() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session != null && session.getAttribute("userId") != null) {
            return "redirect:/dashboard"; // already logged in
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = (User) authentication.getPrincipal();
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                 org.springframework.security.core.context.SecurityContextHolder.getContext());
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().name());

            // Redirect admins to admin panel, regular users to dashboard
            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/admin";
            }
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.warn("Login failed for username='{}': {}", username, e.getMessage());
            request.setAttribute("error", "Invalid username or password. Please try again.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session != null && session.getAttribute("userId") != null) {
            return "redirect:/dashboard"; // already logged in
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 HttpServletRequest request) {
        try {
            authService.register(new com.nietproject.cryptoforge.dto.RegisterRequest(username, email, password));
            return "redirect:/login?registered=1";
        } catch (Exception e) {
            log.warn("Registration failed for username='{}': {}", username, e.getMessage());
            request.setAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Map<String, Object> portfolio = portfolioService.getPortfolio(userId);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("usdBalance", portfolio.get("usdBalance"));
        model.addAttribute("cryptoValue", portfolio.get("cryptoValue"));
        model.addAttribute("totalValue", portfolio.get("totalValue"));
        model.addAttribute("assets", assetRepository.findAllByOrderByMarketCapUsdDesc());
        model.addAttribute("totalTrades", orderRepository.countCompletedOrders(userId));

        return "dashboard";
    }

    @GetMapping("/portfolio")
    public String showPortfolio(@RequestParam(required = false) String success,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Map<String, Object> portfolio = portfolioService.getPortfolio(userId);
        model.addAttribute("usdBalance", portfolio.get("usdBalance"));
        model.addAttribute("cryptoValue", portfolio.get("cryptoValue"));
        model.addAttribute("totalValue", portfolio.get("totalValue"));
        model.addAttribute("holdings", portfolio.get("holdings"));
        model.addAttribute("totalInvested", portfolio.get("totalInvested"));
        model.addAttribute("totalRealized", portfolio.get("totalRealized"));
        if (success != null) {
            model.addAttribute("success", success);
        }

        return "portfolio";
    }

    @GetMapping("/order")
    public String showOrderForm(@RequestParam(required = false) String asset,
                                @RequestParam(required = false) String success,
                                @RequestParam(required = false) String error,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("selectedAsset", asset != null ? asset.toUpperCase() : "BTC");
        model.addAttribute("assets", assetRepository.findAllByOrderByMarketCapUsdDesc());

        BigDecimal usdBalance = walletRepository
                .findByUserIdAndCurrencyCode(userId, "USD")
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
        model.addAttribute("usdBalance", usdBalance);

        if (success != null) model.addAttribute("success", success);
        if (error != null) model.addAttribute("error", error);

        return "orderForm";
    }
    @PostMapping("/order")
    public String executeOrder(@RequestParam(required = false) Long userId,
                               @RequestParam String assetCode,
                               @RequestParam String orderType,
                               @RequestParam BigDecimal quantity,
                               HttpSession session,
                               Model model) {
        Long loggedInUserId = (Long) session.getAttribute("userId");
        if (loggedInUserId == null) {
            return "redirect:/login";
        }
        try {
            OrderRequest req = new OrderRequest();
            req.setUserId(loggedInUserId);
            req.setAssetCode(assetCode);
            req.setOrderType(orderType);
            req.setQuantity(quantity);

            orderService.placeOrder(req);
            return "redirect:/portfolio?success=Order executed successfully!";
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(WebViewController.class).error("Order execution failed for user {}: {}", loggedInUserId, e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("selectedAsset", assetCode);
            model.addAttribute("assets", assetRepository.findAllByOrderByMarketCapUsdDesc());
            BigDecimal usdBalance = walletRepository
                    .findByUserIdAndCurrencyCode(loggedInUserId, "USD")
                    .map(Wallet::getBalance)
                    .orElse(BigDecimal.ZERO);
            model.addAttribute("usdBalance", usdBalance);
            return "orderForm";
        }
    }

    @GetMapping("/transactions")
    public String showTransactionHistory(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("transactions", transactionRepository.findByUserIdOrderByCreatedAtDesc(userId));
        return "transactions";
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        // Data is loaded via /api/leaderboard fetch in JS
        model.addAttribute("username", session.getAttribute("username"));
        return "leaderboard";
    }
}
