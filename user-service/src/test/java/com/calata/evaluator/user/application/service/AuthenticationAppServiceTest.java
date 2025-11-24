package com.calata.evaluator.user.application.service;

import com.calata.evaluator.user.application.command.LoginCommand;
import com.calata.evaluator.user.application.command.LogoutCommand;
import com.calata.evaluator.user.application.command.RegisterUserCommand;
import com.calata.evaluator.user.application.port.out.*;
import com.calata.evaluator.user.domain.model.AuthToken;
import com.calata.evaluator.user.domain.model.Role;
import com.calata.evaluator.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationAppServiceTest {

    @Mock
    private UserReader userReader;

    @Mock
    private UserWriter userWriter;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenEncoder tokenEncoder;

    @Mock
    private TokenStore tokenStore;

    private AuthenticationAppService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationAppService(
                userReader,
                userWriter,
                passwordHasher,
                tokenEncoder,
                tokenStore,
                30L
        );
    }

    // ---------- register(...) ----------

    @Test
    void register_shouldThrowWhenEmailAlreadyInUse() {
        // given
        RegisterUserCommand cmd = mock(RegisterUserCommand.class);
        when(cmd.email()).thenReturn("USER@MAIL.COM");
        when(userReader.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(mock(User.class)));

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.register(cmd)
        );
        assertEquals("Email already in use", ex.getMessage());

        verify(userReader).findByEmail("user@mail.com");
        verifyNoInteractions(passwordHasher, userWriter);
    }

    @Test
    void register_shouldHashPasswordCreateUserAndSaveWhenEmailFree() {
        // given
        RegisterUserCommand cmd = mock(RegisterUserCommand.class);
        when(cmd.email()).thenReturn("USER@MAIL.COM");
        when(cmd.password()).thenReturn("plain-password");
        Role role = Role.USER;
        when(cmd.role()).thenReturn(role);

        when(userReader.findByEmail("user@mail.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("plain-password")).thenReturn("HASHED");

        User newUser = mock(User.class);
        User savedUser = mock(User.class);

        try (MockedStatic<User> mockedUser = mockStatic(User.class)) {
            mockedUser.when(() -> User.newUser("USER@MAIL.COM", "HASHED", role))
                    .thenReturn(newUser);

            when(userWriter.save(newUser)).thenReturn(savedUser);

            // when
            User result = service.register(cmd);

            // then
            assertSame(savedUser, result);
            verify(userReader).findByEmail("user@mail.com");
            verify(passwordHasher).hash("plain-password");
            mockedUser.verify(() -> User.newUser("USER@MAIL.COM", "HASHED", role));
            verify(userWriter).save(newUser);
        }
    }

    // ---------- login(...) ----------

    @Test
    void login_shouldThrowWhenUserNotFound() {
        // given
        LoginCommand cmd = mock(LoginCommand.class);
        when(cmd.email()).thenReturn("user@mail.com");

        when(userReader.findByEmail("user@mail.com")).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.login(cmd)
        );
        assertEquals("Wrong credentials", ex.getMessage());

        verify(userReader).findByEmail("user@mail.com");
        verifyNoInteractions(passwordHasher, tokenEncoder, tokenStore);
    }

    @Test
    void login_shouldThrowWhenPasswordDoesNotMatch() {
        // given
        LoginCommand cmd = mock(LoginCommand.class);
        when(cmd.email()).thenReturn("user@mail.com");
        when(cmd.password()).thenReturn("wrong");

        User user = mock(User.class);
        when(userReader.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", user.passwordHash()))
                .thenReturn(false);

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.login(cmd)
        );
        assertEquals("Wrong credentials", ex.getMessage());

        verify(userReader).findByEmail("user@mail.com");
        verify(passwordHasher).matches("wrong", user.passwordHash());
        verifyNoInteractions(tokenEncoder, tokenStore);
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        // given
        LoginCommand cmd = mock(LoginCommand.class);
        when(cmd.email()).thenReturn("user@mail.com");
        when(cmd.password()).thenReturn("secret");

        User user = mock(User.class);
        when(userReader.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", user.passwordHash()))
                .thenReturn(true);

        when(tokenEncoder.encode(eq(user), any(Instant.class)))
                .thenReturn("ACCESS_TOKEN");

        // when
        AuthToken token = service.login(cmd);

        // then
        assertNotNull(token);
        verify(userReader).findByEmail("user@mail.com");
        verify(passwordHasher).matches("secret", user.passwordHash());
        verify(tokenEncoder).encode(eq(user), any(Instant.class));
        verifyNoInteractions(tokenStore);
    }

    // ---------- logout(...) ----------

    @Test
    void logout_shouldDoNothingWhenTokenNotValid() {
        // given
        LogoutCommand cmd = mock(LogoutCommand.class);
        when(cmd.token()).thenReturn("TOKEN");

        when(tokenEncoder.notValid("TOKEN")).thenReturn(true);

        // when
        service.logout(cmd);

        // then
        verify(tokenEncoder).notValid("TOKEN");
        verifyNoInteractions(tokenStore);
    }

    @Test
    void logout_shouldDoNothingWhenTokenAlreadyRevoked() {
        // given
        LogoutCommand cmd = mock(LogoutCommand.class);
        when(cmd.token()).thenReturn("TOKEN");

        when(tokenEncoder.notValid("TOKEN")).thenReturn(false);
        when(tokenStore.isRevoked("TOKEN")).thenReturn(true);

        // when
        service.logout(cmd);

        // then
        verify(tokenEncoder).notValid("TOKEN");
        verify(tokenStore).isRevoked("TOKEN");
        verify(tokenStore, never()).revoke(anyString(), any());
    }

    @Test
    void logout_shouldRevokeTokenWhenValidAndNotRevoked() {
        // given
        LogoutCommand cmd = mock(LogoutCommand.class);
        when(cmd.token()).thenReturn("TOKEN");

        when(tokenEncoder.notValid("TOKEN")).thenReturn(false);
        when(tokenStore.isRevoked("TOKEN")).thenReturn(false);

        Instant exp = Instant.parse("2024-01-01T10:00:00Z");
        when(tokenEncoder.expiresAt("TOKEN")).thenReturn(exp);

        // when
        service.logout(cmd);

        // then
        verify(tokenEncoder).notValid("TOKEN");
        verify(tokenStore).isRevoked("TOKEN");
        verify(tokenEncoder).expiresAt("TOKEN");
        verify(tokenStore).revoke("TOKEN", exp);
    }
}
