package com.calata.evaluator.user.infrastructure.web.controller;

import com.calata.evaluator.user.application.command.LoginCommand;
import com.calata.evaluator.user.application.command.LogoutCommand;
import com.calata.evaluator.user.application.command.RegisterUserCommand;
import com.calata.evaluator.user.application.port.in.LoginUseCase;
import com.calata.evaluator.user.application.port.in.LogoutUseCase;
import com.calata.evaluator.user.application.port.in.RegisterUserUseCase;
import com.calata.evaluator.user.application.port.out.UserReader;
import com.calata.evaluator.user.infrastructure.web.dto.*;
import com.calata.evaluator.user.infrastructure.web.mapper.WebMappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUC;
    private final LoginUseCase loginUC;
    private final LogoutUseCase logoutUC;

    public AuthController(RegisterUserUseCase registerUC, LoginUseCase loginUC, LogoutUseCase logoutUC) {
        this.registerUC = registerUC;
        this.loginUC = loginUC;
        this.logoutUC = logoutUC;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req){
        var user = registerUC.register(new RegisterUserCommand(
                req.email, req.password, WebMappers.toRole(req.role)
        ));
        return ResponseEntity.status(201).body(WebMappers.toUserResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req){
        var tokens = loginUC.login(new LoginCommand(req.email, req.password));
        return ResponseEntity.ok(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest body,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth){
        String token = (body != null && body.token != null) ? body.token
                : (auth != null && auth.startsWith("Bearer ") ? auth.substring(7) : null);
        if (token == null) return ResponseEntity.badRequest().build();
        logoutUC.logout(new LogoutCommand(token));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth, UserReader reader) {
        var userId = (String) auth.getPrincipal();
        var user = reader.findById(userId).orElseThrow();
        return ResponseEntity.ok(WebMappers.toUserResponse(user));
    }
}
