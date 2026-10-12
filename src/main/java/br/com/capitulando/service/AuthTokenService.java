package br.com.capitulando.service;

import br.com.capitulando.exception.InvalidAuthTokenException;
import br.com.capitulando.model.AuthToken;
import br.com.capitulando.model.AuthTokenPurpose;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.AuthTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuthTokenService {
    private final AuthTokenRepository authTokenRepository;
    private final Duration emailVerificationTtl;
    private final Duration passwordResetTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(
            AuthTokenRepository authTokenRepository,
            @Value("${app.auth.email-verification-ttl:PT24H}") Duration emailVerificationTtl,
            @Value("${app.auth.password-reset-ttl:PT1H}") Duration passwordResetTtl) {
        this.authTokenRepository = authTokenRepository;
        this.emailVerificationTtl = emailVerificationTtl;
        this.passwordResetTtl = passwordResetTtl;
    }

    @Transactional
    public IssuedToken issue(User user, AuthTokenPurpose purpose) {
        Instant now = Instant.now();
        List<AuthToken> activeTokens = authTokenRepository
                .findAllByUser_IdAndPurposeAndConsumedAtIsNull(user.getId(), purpose);
        activeTokens.forEach(token -> token.setConsumedAt(now));
        authTokenRepository.saveAll(activeTokens);

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setPurpose(purpose);
        token.setExpiresAt(now.plus(ttlFor(purpose)));
        authTokenRepository.save(token);

        return new IssuedToken(rawToken, token.getExpiresAt());
    }

    @Transactional
    public AuthToken consume(String rawToken, AuthTokenPurpose purpose) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidAuthTokenException();
        }

        AuthToken token = authTokenRepository
                .findUnconsumedForUpdate(hash(rawToken), purpose)
                .orElseThrow(InvalidAuthTokenException::new);

        Instant now = Instant.now();
        if (token.isExpired(now)) {
            throw new InvalidAuthTokenException();
        }

        token.setConsumedAt(now);
        return authTokenRepository.save(token);
    }

    public String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    private Duration ttlFor(AuthTokenPurpose purpose) {
        return switch (purpose) {
            case EMAIL_VERIFICATION -> emailVerificationTtl;
            case PASSWORD_RESET -> passwordResetTtl;
        };
    }

    public record IssuedToken(String rawToken, Instant expiresAt) {
    }
}
