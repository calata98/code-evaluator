package com.calata.evaluator.user.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class JwksService {
    private final RSAKey publicJwk;

    public JwksService(RSAKey publicJwk) {
        this.publicJwk = publicJwk;
    }

    public String kid() { return publicJwk.getKeyID(); }

    public Map<String,Object> jwksJsonObject() {
        return new JWKSet(publicJwk).toJSONObject(); // keys:[{kty:"RSA", kid:"..."}]
    }
}
