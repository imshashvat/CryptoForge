package com.nietproject.cryptoforge.controller;

import com.nietproject.cryptoforge.model.PriceAlert;
import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.PriceAlertRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AlertController — manages user-defined price alerts.
 * Users can create alerts that fire when a coin crosses a target price.
 */
@Controller
@RequestMapping("/alerts")
public class AlertController {

    private final PriceAlertRepository alertRepository;
    private final AssetRepository      assetRepository;

    public AlertController(PriceAlertRepository alertRepository,
                           AssetRepository assetRepository) {
        this.alertRepository = alertRepository;
        this.assetRepository  = assetRepository;
    }

    /** Show the alerts management page */
    @GetMapping
    public String showAlerts(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        model.addAttribute("activeAlerts",    alertRepository.findByUserIdAndTriggeredFalseOrderByCreatedAtDesc(userId));
        model.addAttribute("triggeredAlerts", alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().filter(PriceAlert::isTriggered).toList());
        model.addAttribute("assets",          assetRepository.findAllByOrderByMarketCapUsdDesc());
        model.addAttribute("activeCount",     alertRepository.countByUserIdAndTriggeredFalse(userId));
        return "alerts";
    }

    /** Create a new price alert */
    @PostMapping("/create")
    public String createAlert(@RequestParam String assetCode,
                              @RequestParam BigDecimal targetPrice,
                              @RequestParam String direction,
                              HttpSession session,
                              RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        PriceAlert alert = new PriceAlert();
        alert.setUserId(userId);
        alert.setAssetCode(assetCode.toUpperCase());
        alert.setAssetName(assetRepository.findById(assetCode.toUpperCase())
            .map(a -> a.getName()).orElse(assetCode));
        alert.setTargetPrice(targetPrice);
        alert.setDirection(PriceAlert.Direction.valueOf(direction.toUpperCase()));
        alertRepository.save(alert);

        ra.addFlashAttribute("success", "Alert created! You'll be notified when " +
            assetCode + " goes " + direction.toLowerCase() + " $" + targetPrice);
        return "redirect:/alerts";
    }

    /** Delete an alert */
    @PostMapping("/delete/{id}")
    public String deleteAlert(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        alertRepository.findById(id).ifPresent(alert -> {
            if (alert.getUserId().equals(userId)) alertRepository.delete(alert);
        });
        ra.addFlashAttribute("success", "Alert removed.");
        return "redirect:/alerts";
    }

    /** API: check alerts for current user (used by JS polling) */
    @GetMapping("/api/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAlerts(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        List<PriceAlert> triggered = alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().filter(PriceAlert::isTriggered).limit(5).toList();
        long activeCount = alertRepository.countByUserIdAndTriggeredFalse(userId);

        return ResponseEntity.ok(Map.of(
            "triggered", triggered.stream().map(a -> Map.of(
                "assetCode", a.getAssetCode(),
                "assetName", a.getAssetName() != null ? a.getAssetName() : a.getAssetCode(),
                "targetPrice", a.getTargetPrice(),
                "direction", a.getDirection().name()
            )).toList(),
            "activeCount", activeCount
        ));
    }
}
