package br.com.capitulando.controller;

import br.com.capitulando.dto.auth.AuthMessageResponse;
import br.com.capitulando.dto.auth.AuthResponse;
import br.com.capitulando.dto.auth.AuthenticatedUserResponse;
import br.com.capitulando.dto.auth.ForgotPasswordRequest;
import br.com.capitulando.dto.auth.LoginRequest;
import br.com.capitulando.dto.auth.RegisterRequest;
import br.com.capitulando.dto.auth.ResendVerificationRequest;
import br.com.capitulando.dto.auth.ResetPasswordRequest;
import br.com.capitulando.dto.auth.VerifyEmailRequest;
import br.com.capitulando.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AuthMessageResponse("Cadastro iniciado. Confira seu email para confirmar a conta."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authenticationService.verifyEmail(request.token());
        return ResponseEntity.ok(new AuthMessageResponse("Email confirmado. Você já pode entrar na sua conta."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthMessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        try {
            authenticationService.resendVerification(request.email());
        } catch (MailException exception) {
            LOGGER.warn("Falha ao enviar email de confirmação.");
            return ResponseEntity.ok(new AuthMessageResponse(
                    "Se a conta existir e ainda não estiver confirmada, enviaremos um novo email de confirmação."));
        }
        return ResponseEntity.ok(new AuthMessageResponse(
                "Se a conta existir e ainda não estiver confirmada, enviaremos um novo email de confirmação."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> currentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authenticationService.currentUser(UUID.fromString(jwt.getSubject())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authenticationService.requestPasswordReset(request.email());
        } catch (MailException exception) {
            LOGGER.warn("Falha ao enviar email de recuperação de senha.");
            return ResponseEntity.ok(new AuthMessageResponse(
                    "Se existir uma conta confirmada para esse email, enviaremos as instruções de redefinição."));
        }
        return ResponseEntity.ok(new AuthMessageResponse(
                "Se existir uma conta confirmada para esse email, enviaremos as instruções de redefinição."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new AuthMessageResponse("Senha redefinida com sucesso."));
    }
}
    