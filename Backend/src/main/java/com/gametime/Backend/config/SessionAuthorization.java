package com.gametime.Backend.config;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SessionAuthorization {

    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";

    private final UserRepository userRepository;

    public User requireUser(HttpSession session) {
        Object value = session.getAttribute(USER_ID_ATTRIBUTE);
        if (!(value instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Please log in"));
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
        return user;
    }

    public User requireAdmin(HttpSession session) {
        User user = requireUser(session);
        if (user.getRole() != User.UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required");
        }
        return user;
    }
}
