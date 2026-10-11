package br.com.capitulando.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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
        @Value("${app.jwt.issuer:capitulando-api}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
