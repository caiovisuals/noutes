package com.noutes.controller;

import com.noutes.dto.*;
import com.noutes.entity.User;
import com.noutes.repository.UserRepository;
import com.noutes.security.JwtService;
import com.noutes.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var user = User.builder()
            .name(req.name())
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .build();

        userRepository.save(user);

        var refreshToken = refreshTokenService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(jwtService.generate(user), refreshToken.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var user = userRepository.findByEmail(req.email()).orElseThrow();

        refreshTokenService.revokeAllForUser(user);
        var refreshToken = refreshTokenService.create(user);

        return ResponseEntity.ok(new AuthResponse(jwtService.generate(user), refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        var existing = refreshTokenService.validate(req.refreshToken());
        var user = existing.getUser();

        refreshTokenService.revokeAllForUser(user);
        var newRefresh = refreshTokenService.create(user);

        return ResponseEntity.ok(new AuthResponse(jwtService.generate(user), newRefresh.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        refreshTokenService.revokeAllForUser(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<UserDto> validate(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UpdateProfileRequest req,
                                                  @AuthenticationPrincipal User user) {
        if (req.name() != null && !req.name().isBlank()) {
            user.setName(req.name().strip());
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar().isBlank() ? null : req.avatar().strip());
        }
        userRepository.save(user);
        return ResponseEntity.ok(UserDto.from(user));
    }

    public record RefreshRequest(String refreshToken) {}

    public record UpdateProfileRequest(String name, String avatar) {}
}