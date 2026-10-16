package br.com.capitulando.service;

import br.com.capitulando.dto.auth.RegisterRequest;
import br.com.capitulando.exception.AccountConflictException;
import br.com.capitulando.model.AuthToken;
import br.com.capitulando.model.AuthTokenPurpose;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthTokenService authTokenService;
    @InjectMocks
    private AccountService service;

    @Test
    void register_deveNormalizarDadosCriarPerfilEEmitirToken() {
        String email = "writer@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(profileRepository.existsByUsernameIgnoreCase("escritor")).thenReturn(false);
        when(passwordEncoder.encode("Senha123")).thenReturn("hash-da-senha");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Instant expiry = Instant.now().plusSeconds(3600);
        when(authTokenService.issue(any(User.class), eq(AuthTokenPurpose.EMAIL_VERIFICATION)))
                .thenReturn(new AuthTokenService.IssuedToken("token-confirmacao", expiry));

        AccountService.RegistrationResult result = service.register(
                new RegisterRequest(" Escritor ", " WRITER@EXAMPLE.COM ", "Senha123"));

        assertEquals(email, result.email());
        assertEquals("escritor", result.username());
        assertEquals("token-confirmacao", result.verificationToken());
        assertEquals(expiry, result.tokenExpiresAt());
        verify(passwordEncoder).encode("Senha123");
        verify(profileRepository).save(any(Profile.class));
        verify(authTokenService).issue(any(User.class), eq(AuthTokenPurpose.EMAIL_VERIFICATION));
    }

    @Test
    void register_deveRejeitarEmailDuplicado() {
        when(userRepository.existsByEmailIgnoreCase("writer@example.com")).thenReturn(true);

        assertThrows(AccountConflictException.class,
                () -> service.register(new RegisterRequest("escritor", "writer@example.com", "Senha123")));

        verifyNoInteractions(profileRepository, passwordEncoder, authTokenService);
    }

    @Test
    void verifyEmail_deveMarcarContaComoConfirmada() {
        User user = new User();
        user.setEmail("writer@example.com");
        user.setEmailVerified(false);
        AuthToken token = new AuthToken();
        token.setUser(user);
        when(authTokenService.consume("valid-token", AuthTokenPurpose.EMAIL_VERIFICATION)).thenReturn(token);

        service.verifyEmail("valid-token");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void createVerificationToken_deveEmitirTokenSomenteParaContaNaoConfirmada() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("writer@example.com");
        user.setEmailVerified(false);
        AuthTokenService.IssuedToken issued = new AuthTokenService.IssuedToken("verify",
                Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));
        when(authTokenService.issue(user, AuthTokenPurpose.EMAIL_VERIFICATION)).thenReturn(issued);

        assertEquals(Optional.of(issued), service.createVerificationToken(" WRITER@EXAMPLE.COM "));
        verify(authTokenService).issue(user, AuthTokenPurpose.EMAIL_VERIFICATION);
    }

    @Test
    void createVerificationToken_deveRetornarVazioParaContaJaConfirmada() {
        User user = new User();
        user.setEmail("writer@example.com");
        user.setEmailVerified(true);
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));

        assertTrue(service.createVerificationToken("writer@example.com").isEmpty());
        verifyNoInteractions(authTokenService);
    }

    @Test
    void createPasswordResetToken_deveEmitirTokenParaContaConfirmada() {
        User user = new User();
        user.setEmail("writer@example.com");
        user.setEmailVerified(true);
        AuthTokenService.IssuedToken issued = new AuthTokenService.IssuedToken("reset", Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));
        when(authTokenService.issue(user, AuthTokenPurpose.PASSWORD_RESET)).thenReturn(issued);

        assertEquals(Optional.of(issued), service.createPasswordResetToken("writer@example.com"));
        verify(authTokenService).issue(user, AuthTokenPurpose.PASSWORD_RESET);
    }

    @Test
    void createPasswordResetToken_deveRetornarVazioParaContaNaoConfirmada() {
        User user = new User();
        user.setEmail("writer@example.com");
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));

        assertTrue(service.createPasswordResetToken("writer@example.com").isEmpty());
        verifyNoInteractions(authTokenService);
    }

    @Test
    void resetPassword_deveAlterarHashIncrementarVersaoEGuardarUsuario() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("writer@example.com");
        user.setPasswordHash("hash-antigo");
        user.setTokenVersion(2);
        AuthToken token = new AuthToken();
        token.setUser(user);
        when(authTokenService.consume("reset-token", AuthTokenPurpose.PASSWORD_RESET)).thenReturn(token);
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("hash-novo");

        service.resetPassword("reset-token", "NovaSenha123");

        assertEquals("hash-novo", user.getPasswordHash());
        assertEquals(3, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_deveRejeitarSenhaComMaisDe72BytesAntesDeConsumirToken() {
        String oversizedPassword = "a".repeat(73);

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("reset-token", oversizedPassword));

        verifyNoInteractions(authTokenService, passwordEncoder, userRepository);
    }
}
