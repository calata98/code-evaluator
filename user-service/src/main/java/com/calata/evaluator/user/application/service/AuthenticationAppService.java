package com.calata.evaluator.user.application.service;

import com.calata.evaluator.user.application.command.LoginCommand;
import com.calata.evaluator.user.application.command.LogoutCommand;
import com.calata.evaluator.user.application.command.RegisterUserCommand;
import com.calata.evaluator.user.application.port.in.LoginUseCase;
import com.calata.evaluator.user.application.port.in.LogoutUseCase;
import com.calata.evaluator.user.application.port.in.RegisterUserUseCase;
import com.calata.evaluator.user.application.port.out.*;
import com.calata.evaluator.user.domain.model.AuthToken;
import com.calata.evaluator.user.domain.model.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AuthenticationAppService implements RegisterUserUseCase, LoginUseCase, LogoutUseCase {

    private final UserReader userReader;
    private final UserWriter userWriter;
    private final PasswordHasher passwordHasher;
    private final TokenEncoder tokenEncoder;
    private final TokenStore tokenStore;

    // configurable access token duration in minutes
    private final long accessMinutes;

    public AuthenticationAppService(UserReader userReader,
            UserWriter userWriter,
            PasswordHasher passwordHasher,
            TokenEncoder tokenEncoder,
            TokenStore tokenStore,
            long accessMinutes) {
        this.userReader = userReader;
        this.userWriter = userWriter;
        this.passwordHasher = passwordHasher;
        this.tokenEncoder = tokenEncoder;
        this.tokenStore = tokenStore;
        this.accessMinutes = accessMinutes;
    }

    @Override
    public User register(RegisterUserCommand cmd) {
        userReader.findByEmail(cmd.email().toLowerCase()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use");
        });
        String hash = passwordHasher.hash(cmd.password());
        User user = User.newUser(cmd.email(), hash, cmd.role());
        return userWriter.save(user);
    }

    @Override
    public AuthToken login(LoginCommand cmd) {
        var user = userReader.findByEmail(cmd.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Wrong credentials"));
        if (!passwordHasher.matches(cmd.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Wrong credentials");
        }
        Instant exp = Instant.now().plus(accessMinutes, ChronoUnit.MINUTES);
        String access = tokenEncoder.encode(user, exp);
        return new AuthToken(access);
    }

    @Override
    public void logout(LogoutCommand cmd) {
        if (!tokenEncoder.isValid(cmd.token())) return;
        if (tokenStore.isRevoked(cmd.token())) return;
        tokenStore.revoke(cmd.token(), tokenEncoder.expiresAt(cmd.token()));
    }
}
