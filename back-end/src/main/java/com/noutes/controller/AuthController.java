package com.noutes.controller;

import com.noutes.dto.*;
import com.noutes.entity.User;
import com.noutes.repository.UserRepository;
import com.noutes.security.JwtService;
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(jwtService.generate(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        var user = userRepository.findByEmail(req.email()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(jwtService.generate(user)));
    }

    @GetMapping("/validate")
    public ResponseEntity<UserDto> validate(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }
}