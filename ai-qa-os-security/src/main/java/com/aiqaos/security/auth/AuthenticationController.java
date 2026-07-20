package com.aiqaos.security.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(name = "aiqaos.security.database-enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO request,
                                                  @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                                  @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        String ip = truncate((ipAddress != null) ? ipAddress : "127.0.0.1", 100);
        // Truncated to the browser column width: an over-long header must never
        // fail an otherwise valid login (see V11__widen_user_session_browser.sql).
        String browser = truncate((userAgent != null) ? userAgent : "Unknown", 255);
        TokenResponseDTO response = authenticationService.login(request.getUsername(), request.getPassword(), ip, browser, "Desktop");
        return ResponseEntity.ok(response);
    }

    private static String truncate(String value, int maxLength) {
        return (value != null && value.length() > maxLength) ? value.substring(0, maxLength) : value;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenResponseDTO request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody TokenResponseDTO request) {
        TokenResponseDTO response = authenticationService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
