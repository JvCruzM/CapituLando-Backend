package br.com.capitulando.service;

import br.com.capitulando.dto.auth.RegisterRequest;
import br.com.capitulando.exception.AccountConflictException;
import br.com.capitulando.model.AuthTokenPurpose;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AccountService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    @Transactional
    public RegistrationResult register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        validatePasswordLength(request.password());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AccountConflictException("Já existe uma conta com este email.");
        }
        if (profileRepository.existsByUsernameIgnoreCase(username)) {
            throw new AccountConflictException("Este nome de usuário já está em uso.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        User savedUser = userRepository.saveAndFlush(user);

        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setUsername(username);
        profile.setDisplayName(request.username().trim());
        savedUser.setProfile(profile);
        Profile savedProfile = profileRepository.save(profile);

        AuthTokenService.IssuedToken verificationToken = authTokenService
                .issue(savedUser, AuthTokenPurpose.EMAIL_VERIFICATION);

        return new RegistrationResult(
                savedUser.getId(),
                savedUser.getEmail(),
                savedProfile.getUsername(),
                verificationToken.rawToken(),
                verificationToken.expiresAt());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        var token = authTokenService.consume(rawToken, AuthTokenPurpose.EMAIL_VERIFICATION);
        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public Optional<AuthTokenService.IssuedToken> createVerificationToken(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(user -> !user.isEmailVerified())
                .map(user -> authTokenService.issue(user, AuthTokenPurpose.EMAIL_VERIFICATION));
    }

    @Transactional
    public Optional<AuthTokenService.IssuedToken> createPasswordResetToken(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(user -> user.isEmailVerified())
                .map(user -> authTokenService.issue(user, AuthTokenPurpose.PASSWORD_RESET));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        validatePasswordLength(newPassword);
        var token = authTokenService.consume(rawToken, AuthTokenPurpose.PASSWORD_RESET);
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePasswordLength(String password) {
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha deve ter no máximo 72 bytes em UTF-8.");
        }
    }

    public record RegistrationResult(
            UUID userId,
            String email,
            String username,
            String verificationToken,
            Instant tokenExpiresAt) {
    }
}
