package com.gametime.Backend.controllers;

import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.request.CreateWalletTopUpRequest;
import com.gametime.Backend.response.WalletTopUpResponse;
import com.gametime.Backend.service.WalletTopUpService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/topups")
@RequiredArgsConstructor
public class WalletTopUpController {

    private final WalletTopUpService topUpService;
    private final SessionAuthorization sessionAuthorization;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletTopUpResponse submit(@Valid @RequestBody CreateWalletTopUpRequest request,
                                      HttpSession session) {
        Long userId = sessionAuthorization.requireUser(session).getId();
        return topUpService.submit(userId, request);
    }

    @GetMapping
    public List<WalletTopUpResponse> mine(HttpSession session) {
        Long userId = sessionAuthorization.requireUser(session).getId();
        return topUpService.getForUser(userId);
    }
}
