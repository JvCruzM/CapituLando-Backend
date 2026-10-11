package br.com.capitulando.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendUrl;

    public MailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public void sendVerificationEmail(String email, String rawToken) {
        String link = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/confirmar-email")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFromAddress());
        message.setTo(email);
        message.setSubject("Confirme seu email — CapituLando");
        message.setText("Olá! Para confirmar seu email no CapituLando, acesse o link abaixo:\n\n"
                + link + "\n\nSe você não criou esta conta, ignore esta mensagem.");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String email, String rawToken) {
        String link = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/redefinir-senha")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getFromAddress());
        message.setTo(email);
        message.setSubject("Redefinição de senha — CapituLando");
        message.setText("Recebemos uma solicitação para redefinir sua senha no CapituLando.\n\n"
                + "Para criar uma nova senha, acesse o link abaixo:\n\n"
                + link + "\n\nSe você não solicitou esta alteração, ignore esta mensagem.");
        mailSender.send(message);
    }

    private String getFromAddress() {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new MailSendException("Configure MAIL_FROM ou GMAIL_USERNAME antes de enviar emails.");
        }
        return fromAddress;
    }
}
