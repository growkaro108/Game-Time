package com.gametime.Backend.service;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.entity.WalletTopUpRequest;
import com.gametime.Backend.entity.WalletTopUpRequest.TopUpStatus;
import com.gametime.Backend.repository.UserRepository;
import com.gametime.Backend.repository.WalletTopUpRequestRepository;
import com.gametime.Backend.request.CreateWalletTopUpRequest;
import com.gametime.Backend.response.WalletTopUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletTopUpService {

    private final WalletTopUpRequestRepository topUpRepository;
    private final UserRepository userRepository;

    @Transactional
    public WalletTopUpResponse submit(Long userId, CreateWalletTopUpRequest request) {
        String transactionId = request.transactionId().trim();
        if (topUpRepository.existsByTransactionId(transactionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This transaction ID has already been submitted");
        }
        User user = findUser(userId);
        WalletTopUpRequest topUp = WalletTopUpRequest.builder()
                .user(user)
                .amountInr(request.amountInr())
                .paymentMethod(request.paymentMethod().trim())
                .transactionId(transactionId)
                .build();
        return WalletTopUpResponse.from(topUpRepository.save(topUp));
    }

    @Transactional(readOnly = true)
    public List<WalletTopUpResponse> getForUser(Long userId) {
        return topUpRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(WalletTopUpResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WalletTopUpResponse> getPending() {
        return topUpRepository.findByStatusOrderByCreatedAtAsc(TopUpStatus.PENDING).stream()
                .map(WalletTopUpResponse::from)
                .toList();
    }

    @Transactional
    public WalletTopUpResponse approve(Long requestId, Long adminId) {
        WalletTopUpRequest request = findForUpdate(requestId);
        ensurePending(request);
        User user = userRepository.findByIdForUpdate(request.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        request.setUser(user);
        user.setInrBalance(user.getInrBalance().add(request.getAmountInr()));
        request.setStatus(TopUpStatus.APPROVED);
        request.setReviewedBy(adminId);
        request.setReviewedAt(LocalDateTime.now());
        userRepository.save(user);
        return WalletTopUpResponse.from(topUpRepository.save(request));
    }

    @Transactional
    public WalletTopUpResponse reject(Long requestId, Long adminId) {
        WalletTopUpRequest request = findForUpdate(requestId);
        ensurePending(request);
        request.setStatus(TopUpStatus.REJECTED);
        request.setReviewedBy(adminId);
        request.setReviewedAt(LocalDateTime.now());
        return WalletTopUpResponse.from(topUpRepository.save(request));
    }

    private WalletTopUpRequest findForUpdate(Long id) {
        return topUpRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Top-up request not found"));
    }

    private void ensurePending(WalletTopUpRequest request) {
        if (request.getStatus() != TopUpStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This top-up request has already been reviewed");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
