package Therapie_Management.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import Therapie_Management.backend.dto.LoginResponse;
import Therapie_Management.backend.dto.RegisterRequest;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.Role;
import Therapie_Management.backend.entity.User;
import Therapie_Management.backend.exception.ConflictException;
import Therapie_Management.backend.repository.UserRepository;
import Therapie_Management.backend.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private final RegisterRequest request = new RegisterRequest("Anna", "Beispiel", "anna01", "geheim123");

    @Test
    void register_legtPatientMitGehashtemPasswortAn() {
        when(userRepository.existsById("anna01")).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("hash");
        when(jwtTokenProvider.generateToken("anna01", Role.PATIENT)).thenReturn("token");

        LoginResponse response = authService.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue()).isInstanceOf(Patient.class);
        assertThat(saved.getValue().getPasswortHash()).isEqualTo("hash");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.PATIENT);
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.userId()).isEqualTo("anna01");
    }

    @Test
    void register_wirftConflict_wennBenutzernameVergeben() {
        when(userRepository.existsById("anna01")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }
}
