package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.application.port.out.TokenEncoder;
import com.calata.evaluator.user.domain.model.Role;
import com.calata.evaluator.user.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    private String privatePem;
    private String publicPem;

    private JwtTokenProvider jwt;

    @BeforeEach
    void setup() throws Exception {
        // Generate RSA keypair for testing
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        privatePem = toPrivatePem(pair.getPrivate());
        publicPem  = toPublicPem(pair.getPublic());

        jwt = new JwtTokenProvider(privatePem, publicPem,
                "my-issuer", "test-kid");
    }

    // -------- Tests --------

    @Test
    void encode_shouldGenerateValidJwtAndContainClaims() {
        // given
        User user = mock(User.class);
        when(user.id()).thenReturn("user-123");
        when(user.email()).thenReturn("test@mail.com");
        when(user.role()).thenReturn(Role.ADMIN);

        Instant exp = Instant.now().plus(1, ChronoUnit.HOURS);

        // when
        String token = jwt.encode(user, exp);

        // then
        assertNotNull(token);

        Claims claims = parse(token);

        assertEquals("my-issuer", claims.getIssuer());
        assertEquals("user-123", claims.getSubject());
        assertEquals("test@mail.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));

        assertEquals(exp.truncatedTo(ChronoUnit.SECONDS),
                claims.getExpiration().toInstant().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void notValid_shouldReturnFalseForValidToken() {
        User u = mock(User.class);
        when(u.id()).thenReturn("id");
        when(u.email()).thenReturn("e@e.com");
        when(u.role()).thenReturn(Role.USER);

        String token = jwt.encode(u, Instant.now().plus(10, ChronoUnit.MINUTES));

        assertFalse(jwt.notValid(token));
    }

    @Test
    void notValid_shouldReturnTrueForInvalidToken() {
        assertTrue(jwt.notValid("malformed.token.here"));
    }

    @Test
    void expiresAt_shouldReturnExpirationInstant() {
        User user = mock(User.class);
        when(user.id()).thenReturn("id");
        when(user.email()).thenReturn("e");
        when(user.role()).thenReturn(Role.USER);

        Instant exp = Instant.now().plus(45, ChronoUnit.MINUTES);

        String token = jwt.encode(user, exp);

        assertEquals(exp.truncatedTo(ChronoUnit.SECONDS),
                jwt.expiresAt(token).truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void constructor_shouldThrowForInvalidPemPrivateKey() {
        assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider("bad pem", publicPem, "issuer", "kid"));
    }

    @Test
    void constructor_shouldThrowForInvalidPemPublicKey() {
        assertThrows(IllegalStateException.class, () ->
                new JwtTokenProvider(privatePem, "bad pem", "issuer", "kid"));
    }

    // -------- Helpers --------

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(readPublicKeyFromPem(publicPem))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Convert private key to PKCS8 PEM
    private String toPrivatePem(PrivateKey key) {
        String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
                b64 + "\n-----END PRIVATE KEY-----";
    }

    // Convert public key to X.509 PEM
    private String toPublicPem(PublicKey key) {
        String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                b64 + "\n-----END PUBLIC KEY-----";
    }

    private PublicKey readPublicKeyFromPem(String pem) {
        try {
            String cleaned = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(cleaned);
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
