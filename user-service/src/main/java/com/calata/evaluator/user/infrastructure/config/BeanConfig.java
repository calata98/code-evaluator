package com.calata.evaluator.user.infrastructure.config;

import com.calata.evaluator.user.application.port.out.*;
import com.calata.evaluator.user.application.service.AuthenticationAppService;
import com.calata.evaluator.user.infrastructure.security.JwksService;
import com.calata.evaluator.user.infrastructure.security.JwtTokenProvider;
import com.calata.evaluator.user.infrastructure.security.KeyMaterialLoader;
import com.calata.evaluator.user.infrastructure.security.TokenStoreMongoAdapter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class BeanConfig {

    @Bean
    public TokenEncoder tokenEncoder(
            @Value("${security.jwt.privatePemB64:}") String privateB64,
            @Value("${security.jwt.publicPemB64:}")  String publicB64,
            @Value("${security.jwt.privatePemPath:}") String privatePath,
            @Value("${security.jwt.publicPemPath:}")  String publicPath,
            @Value("${security.jwt.issuer}") String issuer,
            RSAKey rsaKey
    ) {
        String privatePem = KeyMaterialLoader.loadPem(privateB64, privatePath);
        String publicPem  = KeyMaterialLoader.loadPem(publicB64, publicPath);
        return new JwtTokenProvider(privatePem, publicPem, issuer, rsaKey.getKeyID());
    }

    @Bean
    public TokenStore tokenStore(MongoTemplate template) {
        return new TokenStoreMongoAdapter(template);
    }

    @Bean
    public AuthenticationAppService authAppService(UserReader reader, UserWriter writer,
            PasswordHasher hasher, TokenEncoder encoder, TokenStore store,
            @Value("${security.jwt.accessMinutes:30}") long accessMinutes) {
        return new AuthenticationAppService(reader, writer, hasher, encoder, store, accessMinutes);
    }

    @Bean
    public JwksService jwksService(RSAKey rsaKey) {
        return new JwksService(rsaKey.toPublicJWK()); // Publica el JWKS con ese mismo kid
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(@Value("${app.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

        return request -> {
            // Authorization header
            String token = delegate.resolve(request);
            if (token != null) return token;

            // Cookie ACCESS_TOKEN
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("ACCESS_TOKEN".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                        return c.getValue();
                    }
                }
            }
            return null;
        };
    }

    @Bean
    public RSAKey rsaKey(
            @Value("${security.jwt.privatePemB64:}") String privateB64,
            @Value("${security.jwt.publicPemB64:}")  String publicB64,
            @Value("${security.jwt.privatePemPath:}") String privatePath,
            @Value("${security.jwt.publicPemPath:}")  String publicPath
    ) throws JOSEException {
        String privatePem = KeyMaterialLoader.loadPem(privateB64, privatePath);
        String publicPem  = KeyMaterialLoader.loadPem(publicB64, publicPath);

        var privateKey = JwtTokenProvider.readPrivateKeyFromPem(privatePem);
        var publicKey  = JwtTokenProvider.readPublicKeyFromPem(publicPem);

        return new RSAKey.Builder((RSAPublicKey) publicKey)
                .privateKey((RSAPrivateKey) privateKey)
                .keyIDFromThumbprint()
                .build();
    }
}
