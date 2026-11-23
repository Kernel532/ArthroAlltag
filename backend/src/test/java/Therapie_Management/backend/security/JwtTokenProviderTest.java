package Therapie_Management.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import Therapie_Management.backend.entity.Role;

class JwtTokenProviderTest {

    private static final String SECRET = "ThisIsADevOnlySecretKeyForTherapieManagementJWT1234567890";

    @Test
    void frischesTokenIstGueltig() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60);

        assertTrue(provider.validateToken(provider.generateToken("v110001", Role.PATIENT)));
    }

    @Test
    void abgelaufenesTokenIstUngueltig() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1);

        assertFalse(provider.validateToken(provider.generateToken("v110001", Role.PATIENT)));
    }
}
