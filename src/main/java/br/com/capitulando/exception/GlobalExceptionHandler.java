package br.com.capitulando.exception;

import br.com.capitulando.dto.auth.AuthMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountConflictException.class)
    public ResponseEntity<AuthMessageResponse> handleAccountConflict(AccountConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidAuthTokenException.class)
    public ResponseEntity<AuthMessageResponse> handleInvalidAuthToken(InvalidAuthTokenException exception) {
        return ResponseEntity.badRequest()
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<AuthMessageResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<AuthMessageResponse> handleEmailNotVerified(EmailNotVerifiedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthMessageResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Dados inválidos.")
                .orElse("Confira os dados enviados e tente novamente.");
        return ResponseEntity.badRequest()
                .body(new AuthMessageResponse(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthMessageResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<AuthMessageResponse> handleResponseStatus(ResponseStatusException exception) {
        String message = exception.getReason() != null ? exception.getReason() : exception.getMessage();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new AuthMessageResponse(message));
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<AuthMessageResponse> handleMailFailure(MailException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AuthMessageResponse("Não foi possível enviar o email agora. Tente novamente mais tarde."));
    }
}
