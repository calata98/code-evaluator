package com.calata.evaluator.user.infrastructure.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class KeyMaterialLoader {
    private KeyMaterialLoader(){}

    public static String loadPem(String b64, String path) {
        try {
            if (b64 != null && !b64.isBlank()) {
                return new String(Base64.getDecoder().decode(b64));
            }
            if (path != null && !path.isBlank()) {
                return Files.readString(Path.of(path));
            }
            throw new IllegalStateException("No key material provided (neither B64 nor PATH).");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load key material", e);
        }
    }
}
