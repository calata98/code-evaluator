package com.calata.evaluator.user.infrastructure.security;

import com.calata.evaluator.user.application.port.out.TokenEncoder;

import com.calata.evaluator.user.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class JwtTokenProvider implements TokenEncoder {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String issuer;
    private final String kid;

    public JwtTokenProvider(String privatePem, String publicPem, String issuer, String kid) {
        this.privateKey = readPrivateKeyFromPem(privatePem);
        this.publicKey  = readPublicKeyFromPem(publicPem);
        this.issuer = issuer;
        this.kid = kid;
    }

    @Override
    public String encode(User user, Instant expiresAt) {
        return Jwts.builder()
                .header().keyId(kid).and()
                .issuer(issuer)
                .subject(user.id())
                .claim("email", user.email())
                .claim("role", user.role().name())
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Override
    public boolean notValid(String token) {
        try {
            claims(token); return false;
        } catch (Exception e){
            return true;
        }
    }

    @Override
    public Instant expiresAt(String token) {
        return claims(token).getExpiration().toInstant();
    }

    private Claims claims(String token){
        return Jwts.parser().verifyWith(publicKey).build()
                .parseSignedClaims(token).getPayload();
    }

    public static PrivateKey readPrivateKeyFromPem(String pem) {
        try {
            String pk = pem.replace("-----BEGIN PRIVATE KEY-----","")
                    .replace("-----END PRIVATE KEY-----","")
                    .replaceAll("\\s","");
            byte[] der = Base64.getDecoder().decode(pk);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) { throw new IllegalStateException("Bad private key", e); }
    }

    public static PublicKey readPublicKeyFromPem(String pem) {
        try {
            String pk = pem.replace("-----BEGIN PUBLIC KEY-----","")
                    .replace("-----END PUBLIC KEY-----","")
                    .replaceAll("\\s","");
            byte[] der = Base64.getDecoder().decode(pk);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) { throw new IllegalStateException("Bad public key", e); }
    }
}
