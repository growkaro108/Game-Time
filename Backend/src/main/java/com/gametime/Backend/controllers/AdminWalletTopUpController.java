package com.gametime.Backend.controllers;

import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.entity.User;
import com.gametime.Backend.response.WalletTopUpResponse;
import com.gametime.Backend.service.WalletTopUpService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/wallet/topups")
@RequiredArgsConstructor
public class AdminWalletTopUpController {

    private final WalletTopUpService topUpService;
    private final SessionAuthorization sessionAuthorization;

    @GetMapping
    public List<WalletTopUpResponse> pending(HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return topUpService.getPending();
    }

    @PostMapping("/{id}/approve")
    public WalletTopUpResponse approve(@PathVariable Long id, HttpSession session) {
        User admin = sessionAuthorization.requireAdmin(session);
        return topUpService.approve(id, admin.getId());
    }

    @PostMapping("/{id}/reject")
    public WalletTopUpResponse reject(@PathVariable Long id, HttpSession session) {
        User admin = sessionAuthorization.requireAdmin(session);
        return topUpService.reject(id, admin.getId());
    }
}
