package br.com.capitulando.service;

import br.com.capitulando.dto.auth.AuthResponse;
import br.com.capitulando.dto.auth.AuthenticatedUserResponse;
import br.com.capitulando.dto.auth.LoginRequest;
import br.com.capitulando.dto.auth.RegisterRequest;
import br.com.capitulando.exception.EmailNotVerifiedException;
import br.com.capitulando.exception.InvalidCredentialsException;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.UserRepository;
import br.com.capitulando.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    public AuthenticationService(
            AccountService accountService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MailService mailService) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        AccountService.RegistrationResult result = accountService.register(request);
        mailService.sendVerificationEmail(result.email(), result.verificationToken());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        accountService.verifyEmail(rawToken);
    }

    @Transactional
    public void resendVerification(String email) {
        accountService.createVerificationToken(email)
                .ifPresent(token -> mailService.sendVerificationEmail(normalizeEmail(email), token.rawToken()));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        AuthenticatedUserResponse authenticatedUser = toResponse(user);
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                "Bearer",
                jwtService.getAccessTokenTtlSeconds(),
                authenticatedUser);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        accountService.createPasswordResetToken(email)
                .ifPresent(token -> mailService.sendPasswordResetEmail(normalizeEmail(email), token.rawToken()));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        accountService.resetPassword(rawToken, newPassword);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
        if (user.getProfile() == null) {
            throw new InvalidCredentialsException();
        }
        return toResponse(user);
    }

    private AuthenticatedUserResponse toResponse(User user) {
        var profile = user.getProfile();
        if (profile == null) {
            throw new InvalidCredentialsException();
        }
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getEmail(),
                user.isEmailVerified(),
                profile.getUsername(),
                profile.getDisplayName());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
