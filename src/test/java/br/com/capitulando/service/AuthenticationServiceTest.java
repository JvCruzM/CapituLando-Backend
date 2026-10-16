package br.com.capitulando.service;

import br.com.capitulando.dto.auth.AuthResponse;
import br.com.capitulando.dto.auth.LoginRequest;
import br.com.capitulando.dto.auth.RegisterRequest;
import br.com.capitulando.exception.EmailNotVerifiedException;
import br.com.capitulando.exception.InvalidCredentialsException;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.UserRepository;
import br.com.capitulando.security.JwtService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private MailService mailService;
    @InjectMocks
    private AuthenticationService service;

    @Test
    void login_deveRetornarJwtParaContaConfirmada() {
        User user = verifiedUserWithProfile();
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha123", "hash-senha")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-de-teste");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(3600L);

        AuthResponse response = service.login(new LoginRequest(" WRITER@EXAMPLE.COM ", "Senha123"));

        assertEquals("jwt-de-teste", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
        assertEquals("writer@example.com", response.user().email());
        assertTrue(response.user().emailVerified());
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void login_deveRejeitarSenhaIncorreta() {
        User user = verifiedUserWithProfile();
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash-senha")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("writer@example.com", "errada")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_deveRejeitarContaNaoConfirmada() {
        User user = verifiedUserWithProfile();
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase("writer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha123", "hash-senha")).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class,
                () -> service.login(new LoginRequest("writer@example.com", "Senha123")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_deveRejeitarUsuarioInexistente() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("missing@example.com", "Senha123")));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void register_deveEnviarEmailDeConfirmacaoAposCriarConta() {
        RegisterRequest request = new RegisterRequest("escritor", "writer@example.com", "Senha123");
        AccountService.RegistrationResult result = new AccountService.RegistrationResult(
                UUID.randomUUID(), "writer@example.com", "escritor", "verification-token",
                Instant.now().plusSeconds(600));
        when(accountService.register(request)).thenReturn(result);

        service.register(request);

        verify(accountService).register(request);
        verify(mailService).sendVerificationEmail("writer@example.com", "verification-token");
    }

    @Test
    void verifyEmail_deveDelegarParaAccountService() {
        service.verifyEmail("verification-token");
        verify(accountService).verifyEmail("verification-token");
    }

    @Test
    void resendVerification_deveEnviarEmailQuandoTokenForEmitido() {
        AuthTokenService.IssuedToken token = new AuthTokenService.IssuedToken("novo-token",
                Instant.now().plusSeconds(600));
        when(accountService.createVerificationToken(" WRITER@EXAMPLE.COM ")).thenReturn(Optional.of(token));

        service.resendVerification(" WRITER@EXAMPLE.COM ");

        verify(mailService).sendVerificationEmail("writer@example.com", "novo-token");
    }

    @Test
    void resendVerification_naoDeveEnviarEmailQuandoNaoHouverToken() {
        when(accountService.createVerificationToken("unknown@example.com")).thenReturn(Optional.empty());

        service.resendVerification("unknown@example.com");

        verifyNoInteractions(mailService);
    }

    @Test
    void requestPasswordReset_deveEnviarEmailQuandoTokenForEmitido() {
        AuthTokenService.IssuedToken token = new AuthTokenService.IssuedToken("reset-token",
                Instant.now().plusSeconds(600));
        when(accountService.createPasswordResetToken(" WRITER@EXAMPLE.COM ")).thenReturn(Optional.of(token));

        service.requestPasswordReset(" WRITER@EXAMPLE.COM ");

        verify(mailService).sendPasswordResetEmail("writer@example.com", "reset-token");
    }

    @Test
    void requestPasswordReset_naoDeveEnviarEmailQuandoContaNaoForElegivel() {
        when(accountService.createPasswordResetToken("unknown@example.com")).thenReturn(Optional.empty());

        service.requestPasswordReset("unknown@example.com");

        verifyNoInteractions(mailService);
    }

    @Test
    void resetPassword_deveDelegarParaAccountService() {
        service.resetPassword("reset-token", "NovaSenha123");
        verify(accountService).resetPassword("reset-token", "NovaSenha123");
    }

    @Test
    void currentUser_deveRetornarDadosDoPerfil() {
        User user = verifiedUserWithProfile();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var result = service.currentUser(user.getId());

        assertEquals(user.getId(), result.id());
        assertEquals("writer@example.com", result.email());
        assertEquals("escritor", result.username());
        assertEquals("Escritor", result.displayName());
    }

    @Test
    void currentUser_deveRejeitarUsuarioSemPerfil() {
        User user = verifiedUserWithProfile();
        user.setProfile(null);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> service.currentUser(user.getId()));
    }

    private User verifiedUserWithProfile() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("writer@example.com");
        user.setPasswordHash("hash-senha");
        user.setEmailVerified(true);
        Profile profile = new Profile();
        profile.setUsername("escritor");
        profile.setDisplayName("Escritor");
        user.setProfile(profile);
        return user;
    }
}
