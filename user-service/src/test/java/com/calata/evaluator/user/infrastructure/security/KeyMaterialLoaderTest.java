package com.calata.evaluator.user.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class KeyMaterialLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadPem_returnsDecodedPem_whenBase64Provided() {
        // given
        String pemContent = "-----BEGIN KEY-----\nABCDEF\n-----END KEY-----";
        String base64 = Base64.getEncoder().encodeToString(pemContent.getBytes(StandardCharsets.UTF_8));

        // when
        String result = KeyMaterialLoader.loadPem(base64, null);

        // then
        assertEquals(pemContent, result);
    }

    @Test
    void loadPem_readsFromFile_whenBase64BlankAndPathProvided() throws IOException {
        // given
        String pemContent = "-----BEGIN KEY-----\nFROM_FILE\n-----END KEY-----";
        Path pemFile = tempDir.resolve("key.pem");
        Files.writeString(pemFile, pemContent, StandardCharsets.UTF_8);

        // when
        String result = KeyMaterialLoader.loadPem("   ", pemFile.toString());

        // then
        assertEquals(pemContent, result);
    }

    @Test
    void loadPem_throwsIllegalStateException_whenNoKeyMaterialProvided() {
        // when
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> KeyMaterialLoader.loadPem(null, null)
        );

        // then
        assertEquals("Failed to load key material", ex.getMessage());
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertEquals(
                "No key material provided (neither B64 nor PATH).",
                ex.getCause().getMessage()
        );
    }

    @Test
    void loadPem_wrapsException_whenFileCannotBeRead() {
        // given
        Path missingFile = tempDir.resolve("nonexistent.pem");

        // when
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> KeyMaterialLoader.loadPem(null, missingFile.toString())
        );

        // then
        assertEquals("Failed to load key material", ex.getMessage());
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IOException);
    }
}
