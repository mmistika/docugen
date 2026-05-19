package com.artembilous.docugen.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenHasherTest {

    @Test
    void hash_ShouldReturnCorrectSha256Hex() {
        String rawToken = "dg_test_token_12345";
        String expectedHash = "d42a34beed5699a5bb22e32d9a4d10c895f12461cb9504a04fc96571fe91456e"; // SHA-256 of "dg_test_token_12345"

        String actualHash = TokenHasher.hash(rawToken);

        assertNotNull(actualHash);
        assertEquals(expectedHash, actualHash);
    }

    @Test
    void hash_ShouldReturnDifferentHashesForDifferentInputs() {
        String token1 = "token1";
        String token2 = "token2";

        assertNotEquals(TokenHasher.hash(token1), TokenHasher.hash(token2));
    }
}
