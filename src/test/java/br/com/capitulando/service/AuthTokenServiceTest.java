package br.com.capitulando.service;

import br.com.capitulando.exception.InvalidAuthTokenException;
import br.com.capitulando.model.AuthToken;
import br.com.capitulando.model.AuthTokenPurpose;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository repository;

    private AuthTokenService service;

    @BeforeEach
    void setUp() {
        service = new AuthTokenService(repository, Duration.ofHours(24), Duration.ofHours(1));
    }

    @Test
    void issue_deveGerarTokenSeguroArmazenarHashEDefinirExpiracao() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(repository.findAllByUser_IdAndPurposeAndConsumedAtIsNull(user.getId(), AuthTokenPurpose.PASSWORD_RESET))
                .thenReturn(List.of());
        when(repository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        AuthTokenService.IssuedToken result = service.issue(user, AuthTokenPurpose.PASSWORD_RESET);

        Instant after = Instant.now();
        ArgumentCaptor<AuthToken> captor = ArgumentCaptor.forClass(AuthToken.class);
        verify(repository).save(captor.capture());
        AuthToken stored = captor.getValue();
        assertNotNull(result.rawToken());
        assertFalse(result.rawToken().isBlank());
        assertEquals(service.hash(result.rawToken()), stored.getTokenHash());
        assertNotEquals(result.rawToken(), stored.getTokenHash());
        assertEquals(AuthTokenPurpose.PASSWORD_RESET, stored.getPurpose());
        assertTrue(result.expiresAt().isAfter(before.plusSeconds(3590)));
        assertTrue(result.expiresAt().isBefore(after.plusSeconds(3610)));
    }

    @Test
    void issue_deveInvalidarTokensAtivosAnterioresDoMesmoTipo() {
        User user = new User();
        user.setId(UUID.randomUUID());
        AuthToken previous = new AuthToken();
        previous.setUser(user);
        when(repository.findAllByUser_IdAndPurposeAndConsumedAtIsNull(user.getId(),
                AuthTokenPurpose.EMAIL_VERIFICATION))
                .thenReturn(List.of(previous));
        when(repository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.issue(user, AuthTokenPurpose.EMAIL_VERIFICATION);

        assertNotNull(previous.getConsumedAt());
        verify(repository).saveAll(List.of(previous));
        verify(repository).save(any(AuthToken.class));
    }

    @Test
    void consume_deveMarcarTokenValidoComoUtilizado() {
        String rawToken = "token-valido";
        AuthToken token = new AuthToken();
        token.setTokenHash(service.hash(rawToken));
        token.setPurpose(AuthTokenPurpose.PASSWORD_RESET);
        token.setExpiresAt(Instant.now().plusSeconds(600));
        when(repository.findUnconsumedForUpdate(service.hash(rawToken), AuthTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(repository.save(token)).thenReturn(token);

        AuthToken result = service.consume(rawToken, AuthTokenPurpose.PASSWORD_RESET);

        assertSame(token, result);
        assertNotNull(token.getConsumedAt());
        verify(repository).save(token);
    }

    @Test
    void consume_deveRejeitarTokenVazioSemConsultarRepositorio() {
        assertThrows(InvalidAuthTokenException.class,
                () -> service.consume("  ", AuthTokenPurpose.PASSWORD_RESET));
        verifyNoInteractions(repository);
    }

    @Test
    void consume_deveRejeitarTokenNaoEncontrado() {
        when(repository.findUnconsumedForUpdate(service.hash("inexistente"), AuthTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThrows(InvalidAuthTokenException.class,
                () -> service.consume("inexistente", AuthTokenPurpose.PASSWORD_RESET));
        verify(repository, never()).save(any(AuthToken.class));
    }

    @Test
    void consume_deveRejeitarTokenExpirado() {
        String rawToken = "token-expirado";
        AuthToken token = new AuthToken();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        when(repository.findUnconsumedForUpdate(service.hash(rawToken), AuthTokenPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidAuthTokenException.class,
                () -> service.consume(rawToken, AuthTokenPurpose.PASSWORD_RESET));
        verify(repository, never()).save(any(AuthToken.class));
    }

    @Test
    void hash_deveProduzirHashSha256DeterministicoDe64Caracteres() {
        String first = service.hash("same-token");
        String second = service.hash("same-token");

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertNotEquals(service.hash("different-token"), first);
    }
}
