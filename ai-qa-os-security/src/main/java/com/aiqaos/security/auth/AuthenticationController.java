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
        String ip = (ipAddress != null) ? ipAddress : "127.0.0.1";
        String browser = (userAgent != null) ? userAgent : "Unknown";
        TokenResponseDTO response = authenticationService.login(request.getUsername(), request.getPassword(), ip, browser, "Desktop");
        return ResponseEntity.ok(response);
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
