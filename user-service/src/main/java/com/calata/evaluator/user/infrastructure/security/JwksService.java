package com.calata.evaluator.user.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class JwksService {

    private final JWKSet jwkSet;
    private final String kid;


    public JwksService(String publicPem) {
        RSAKey rsaPublicJwk = buildRsaPublicJwk(publicPem);
        this.kid = rsaPublicJwk.getKeyID();
        this.jwkSet = new JWKSet(rsaPublicJwk);
    }

    public JwksService(List<String> publicPems) {
        List<JWK> keys = publicPems.stream().map(JwksService::buildRsaPublicJwk).map(j -> (JWK) j).toList();
        this.jwkSet = new JWKSet(keys);
        this.kid = keys.isEmpty() ? null : keys.get(0).getKeyID();
    }

    public String kid() { return kid; }

    public Map<String, Object> jwksJsonObject() {
        return jwkSet.toJSONObject();
    }

    private static RSAKey buildRsaPublicJwk(String publicPem) {
        try {
            String body = publicPem.replace("-----BEGIN PUBLIC KEY-----","")
                    .replace("-----END PUBLIC KEY-----","")
                    .replaceAll("\\s","");
            byte[] der = Base64.getDecoder().decode(body);
            var pub = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));

            return new RSAKey.Builder(pub)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyIDFromThumbprint()
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA public key (PEM)", e);
        }
    }
}
