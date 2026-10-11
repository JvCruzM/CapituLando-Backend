package br.com.capitulando.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Confirme seu email antes de entrar.");
    }
}
