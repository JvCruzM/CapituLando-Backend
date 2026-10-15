package br.com.capitulando.config;

import br.com.capitulando.repository.UserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtConfiguration {
    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret-base64:}") String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("A variável APP_JWT_SECRET_BASE64 precisa estar configurada.");
        }

        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(secretBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("APP_JWT_SECRET_BASE64 precisa conter uma chave Base64 válida.");
        }

        if (secretBytes.length < 32) {
            throw new IllegalStateException("A chave JWT precisa ter pelo menos 32 bytes após a decodificação Base64.");
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            @Value("${app.jwt.issuer:capitulando-api}") String issuer,
            UserRepository userRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> tokenVersionValidator = jwt -> {
            try {
                UUID userId = UUID.fromString(jwt.getSubject());
                Object claim = jwt.getClaims().get("tokenVersion");
                if (!(claim instanceof Number tokenVersion)) {
                    return invalidTokenResult();
                }

                return userRepository.findById(userId)
                        .filter(user -> user.isEmailVerified())
                        .filter(user -> user.getTokenVersion() == tokenVersion.intValue())
                        .map(user -> OAuth2TokenValidatorResult.success())
                        .orElseGet(JwtConfiguration::invalidTokenResult);
            } catch (IllegalArgumentException | NullPointerException exception) {
                return invalidTokenResult();
            }
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, tokenVersionValidator));
        return decoder;
    }

    private static OAuth2TokenValidatorResult invalidTokenResult() {
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Token inválido ou sessão encerrada.", null));
    }
}
