package com.calata.evaluator.user.infrastructure.config;

import com.calata.evaluator.user.application.port.out.*;
import com.calata.evaluator.user.application.service.AuthenticationAppService;
import com.calata.evaluator.user.infrastructure.security.JwksService;
import com.calata.evaluator.user.infrastructure.security.JwtTokenProvider;
import com.calata.evaluator.user.infrastructure.security.KeyMaterialLoader;
import com.calata.evaluator.user.infrastructure.security.TokenStoreMongoAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration
public class BeanConfig {

    @Bean
    public TokenEncoder tokenEncoder(
            @Value("${security.jwt.privatePemB64:}") String privateB64,
            @Value("${security.jwt.publicPemB64:}")  String publicB64,
            @Value("${security.jwt.privatePemPath:}") String privatePath,
            @Value("${security.jwt.publicPemPath:}")  String publicPath,
            @Value("${security.jwt.issuer}") String issuer,
            JwksService jwksService) {

        String privatePem = KeyMaterialLoader.loadPem(privateB64, privatePath);
        String publicPem  = KeyMaterialLoader.loadPem(publicB64, publicPath);
        return new JwtTokenProvider(privatePem, publicPem, issuer, jwksService.kid());
    }

    @Bean
    public TokenStore tokenStore(MongoTemplate template) { return new TokenStoreMongoAdapter(template); }

    @Bean
    public AuthenticationAppService authAppService(UserReader reader, UserWriter writer,
            PasswordHasher hasher, TokenEncoder encoder, TokenStore store,
            @Value("${security.jwt.accessMinutes:30}") long accessMinutes) {
        return new AuthenticationAppService(reader, writer, hasher, encoder, store, accessMinutes);
    }

    @Bean
    public JwksService jwksService(
            @Value("${security.jwt.publicPemB64:}")  String publicB64,
            @Value("${security.jwt.publicPemPath:}") String publicPath
    ) {
        String publicPem = KeyMaterialLoader.loadPem(publicB64, publicPath);
        return new JwksService(publicPem);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(@Value("${app.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }
}
