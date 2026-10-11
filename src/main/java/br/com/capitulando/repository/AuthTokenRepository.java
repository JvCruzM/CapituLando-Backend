package br.com.capitulando.repository;

import br.com.capitulando.model.AuthToken;
import br.com.capitulando.model.AuthTokenPurpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from AuthToken token where token.tokenHash = :tokenHash and token.purpose = :purpose and token.consumedAt is null")
    Optional<AuthToken> findUnconsumedForUpdate(
            @Param("tokenHash") String tokenHash,
            @Param("purpose") AuthTokenPurpose purpose);

    List<AuthToken> findAllByUser_IdAndPurposeAndConsumedAtIsNull(UUID userId, AuthTokenPurpose purpose);
}
