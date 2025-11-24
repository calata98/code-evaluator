package com.calata.evaluator.user.infrastructure.web.controller;

import com.calata.evaluator.user.application.command.LoginCommand;
import com.calata.evaluator.user.application.command.LogoutCommand;
import com.calata.evaluator.user.application.command.RegisterUserCommand;
import com.calata.evaluator.user.application.port.in.LoginUseCase;
import com.calata.evaluator.user.application.port.in.LogoutUseCase;
import com.calata.evaluator.user.application.port.in.RegisterUserUseCase;
import com.calata.evaluator.user.application.port.out.UserReader;
import com.calata.evaluator.user.domain.model.AuthToken;
import com.calata.evaluator.user.domain.model.User;
import com.calata.evaluator.user.infrastructure.web.dto.LoginRequest;
import com.calata.evaluator.user.infrastructure.web.dto.LogoutRequest;
import com.calata.evaluator.user.infrastructure.web.dto.RegisterRequest;
import com.calata.evaluator.user.infrastructure.web.dto.TokenResponse;
import com.calata.evaluator.user.infrastructure.web.dto.UserResponse;
import com.calata.evaluator.user.infrastructure.web.mapper.WebMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterUserUseCase registerUC;

    @Mock
    private LoginUseCase loginUC;

    @Mock
    private LogoutUseCase logoutUC;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(registerUC, loginUC, logoutUC);
    }

    // ---------- /register ----------

    @Test
    void register_shouldCallUseCaseAndReturn201WithMappedUser() {
        // given
        RegisterRequest req = new RegisterRequest("user@mail.com", "secret", "STUDENT");

        User user = mock(User.class);
        UserResponse userResponse = mock(UserResponse.class);

        when(registerUC.register(any(RegisterUserCommand.class))).thenReturn(user);

        try (MockedStatic<WebMappers> webMappers = mockStatic(WebMappers.class)) {
            webMappers.when(() -> WebMappers.toUserResponse(user))
                    .thenReturn(userResponse);

            // when
            ResponseEntity<UserResponse> res = controller.register(req);

            // then
            assertEquals(201, res.getStatusCode().value());
            assertSame(userResponse, res.getBody());

            ArgumentCaptor<RegisterUserCommand> cmdCaptor =
                    ArgumentCaptor.forClass(RegisterUserCommand.class);
            verify(registerUC).register(cmdCaptor.capture());

            RegisterUserCommand cmd = cmdCaptor.getValue();
            assertEquals("user@mail.com", cmd.email());
            assertEquals("secret", cmd.password());

            webMappers.verify(() -> WebMappers.toUserResponse(user));
        }
    }

    // ---------- /login ----------

    @Test
    void login_shouldCallUseCaseSetCookieAndReturnTokenResponse() {
        // given
        LoginRequest req = new LoginRequest("user@mail.com", "secret");

        AuthToken token = new AuthToken("ACCESS_TOKEN");
        when(loginUC.login(any(LoginCommand.class))).thenReturn(token);

        // when
        ResponseEntity<TokenResponse> res = controller.login(req);

        // then
        assertEquals(200, res.getStatusCode().value());
        TokenResponse body = res.getBody();
        assertNotNull(body);
        assertEquals("ACCESS_TOKEN", body.accessToken());

        String setCookie = res.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("ACCESS_TOKEN=ACCESS_TOKEN"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("SameSite=None"));
        assertTrue(setCookie.contains("Path=/"));
        assertTrue(setCookie.contains("Max-Age=14400"));

        ArgumentCaptor<LoginCommand> cmdCaptor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(loginUC).login(cmdCaptor.capture());
        LoginCommand cmd = cmdCaptor.getValue();
        assertEquals("user@mail.com", cmd.email());
        assertEquals("secret", cmd.password());
    }

    // ---------- /logout ----------

    @Test
    void logout_shouldUseBodyTokenWhenPresent() {
        // given
        LogoutRequest body = new LogoutRequest("TOKEN_BODY");

        // when
        ResponseEntity<Void> res = controller.logout(body, null);

        // then
        assertEquals(204, res.getStatusCode().value());
        ArgumentCaptor<LogoutCommand> cmdCaptor = ArgumentCaptor.forClass(LogoutCommand.class);
        verify(logoutUC).logout(cmdCaptor.capture());
        assertEquals("TOKEN_BODY", cmdCaptor.getValue().token());
    }

    @Test
    void logout_shouldUseAuthorizationHeaderWhenBodyNull() {
        // given
        String authHeader = "Bearer TOKEN_HEADER";

        // when
        ResponseEntity<Void> res = controller.logout(null, authHeader);

        // then
        assertEquals(204, res.getStatusCode().value());
        ArgumentCaptor<LogoutCommand> cmdCaptor = ArgumentCaptor.forClass(LogoutCommand.class);
        verify(logoutUC).logout(cmdCaptor.capture());
        assertEquals("TOKEN_HEADER", cmdCaptor.getValue().token());
    }

    @Test
    void logout_shouldReturnBadRequestWhenNoTokenProvided() {
        // when
        ResponseEntity<Void> res = controller.logout(null, null);

        // then
        assertEquals(400, res.getStatusCode().value());
        verifyNoInteractions(logoutUC);
    }

    // ---------- /me ----------

    @Test
    void me_shouldResolveUserFromAuthenticationAndMapToUserResponse() {
        // given
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("user-id-1");

        UserReader reader = mock(UserReader.class);
        User user = mock(User.class);
        when(reader.findById("user-id-1")).thenReturn(Optional.of(user));

        UserResponse userResponse = mock(UserResponse.class);

        try (MockedStatic<WebMappers> webMappers = mockStatic(WebMappers.class)) {
            webMappers.when(() -> WebMappers.toUserResponse(user))
                    .thenReturn(userResponse);

            // when
            ResponseEntity<UserResponse> res = controller.me(auth, reader);

            // then
            assertEquals(200, res.getStatusCode().value());
            assertSame(userResponse, res.getBody());

            verify(reader).findById("user-id-1");
            webMappers.verify(() -> WebMappers.toUserResponse(user));
        }
    }
}
