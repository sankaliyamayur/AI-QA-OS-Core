package com.aiqaos.security.auth;

import com.aiqaos.security.jwt.JwtTokenProvider;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import com.aiqaos.security.rbac.UserSessionEntity;
import com.aiqaos.security.rbac.UserSessionRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "aiqaos.security.database-enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository,
                                 UserSessionRepository userSessionRepository,
                                 JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public TokenResponseDTO login(String username, String password, String ipAddress, String browser, String device) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (user.isAccountLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Account is locked due to multiple failed login attempts.");
            } else {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid username or password");
        }

        // Reset failed logins on success
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        UUID sessionId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.generateAccessToken(user, sessionId, 1);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, sessionId);

        // Store active session
        UserSessionEntity session = new UserSessionEntity();
        session.setSessionId(sessionId);
        session.setUserId(user.getId());
        session.setIpAddress(ipAddress);
        session.setBrowser(browser);
        session.setDevice(device);
        session.setLoginTime(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        session.setRefreshToken(refreshToken);
        session.setActive(true);
        userSessionRepository.save(session);

        return new TokenResponseDTO(accessToken, refreshToken);
    }

    public void logout(String refreshToken) {
        UserSessionEntity session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        session.setActive(false);
        userSessionRepository.save(session);
    }

    public TokenResponseDTO refresh(String refreshToken) {
        UserSessionEntity session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!session.isActive()) {
            throw new RuntimeException("Session has been terminated");
        }

        UserEntity user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate rotated token pair
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, session.getSessionId(), 1);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, session.getSessionId());

        session.setRefreshToken(newRefreshToken);
        session.setLastActivity(LocalDateTime.now());
        userSessionRepository.save(session);

        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }
}
