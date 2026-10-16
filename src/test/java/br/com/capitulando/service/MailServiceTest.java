package br.com.capitulando.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MailService service;

    @BeforeEach
    void setUp() {
        service = new MailService(mailSender, "noreply@capitulando.test", "https://capitulando.test/");
    }

    @Test
    void sendVerificationEmail_deveEnviarMensagemComLinkDeConfirmacao() {
        service.sendVerificationEmail("writer@example.com", "verify-token-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertEquals("noreply@capitulando.test", message.getFrom());
        assertArrayEquals(new String[] { "writer@example.com" }, message.getTo());
        assertTrue(message.getSubject().contains("Confirme seu email"));
        assertTrue(message.getText().contains("/confirmar-email?token=verify-token-123"));
    }

    @Test
    void sendPasswordResetEmail_deveEnviarMensagemComLinkDeRedefinicao() {
        service.sendPasswordResetEmail("writer@example.com", "reset-token-456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertEquals("noreply@capitulando.test", message.getFrom());
        assertArrayEquals(new String[] { "writer@example.com" }, message.getTo());
        assertTrue(message.getSubject().contains("Redefinição de senha"));
        assertTrue(message.getText().contains("/redefinir-senha?token=reset-token-456"));
    }

    @Test
    void sendEmail_deveFalharQuandoEnderecoDeOrigemNaoEstiverConfigurado() {
        MailService serviceWithoutFrom = new MailService(mailSender, " ", "http://localhost:3000");

        assertThrows(MailSendException.class,
                () -> serviceWithoutFrom.sendVerificationEmail("writer@example.com", "token"));
        verifyNoInteractions(mailSender);
    }
}
