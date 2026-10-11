package br.com.capitulando.exception;

public class InvalidAuthTokenException extends RuntimeException {
    public InvalidAuthTokenException() {
        super("O token é inválido, expirou ou já foi utilizado.");
    }
}
