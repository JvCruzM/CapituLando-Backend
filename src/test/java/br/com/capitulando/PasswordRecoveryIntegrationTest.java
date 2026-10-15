package br.com.capitulando;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.UserRepository;
import br.com.capitulando.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordRecoveryIntegrationTest {

        private static final String OLD_PASSWORD = "CorrectPassword123!";
        private static final String NEW_PASSWORD = "NewSecurePassword987!";
        private static final Pattern ACCESS_TOKEN_PATTERN = Pattern
                        .compile("\\\"accessToken\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @MockitoBean
        private MailService mailService;

        @Test
        void passwordResetChangesPasswordAndTokenCanOnlyBeUsedOnce() throws Exception {
                Account account = createAccount(true);

                MvcResult loginResult = mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody(account.email(), OLD_PASSWORD)))
                                .andExpect(status().isOk())
                                .andReturn();
                String oldJwt = extractAccessToken(loginResult.getResponse().getContentAsString());

                mockMvc.perform(get("/auth/me")
                                .header("Authorization", "Bearer " + oldJwt))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emailBody(account.email())))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                "Se existir uma conta confirmada para esse email, enviaremos as instruções de redefinição.")));

                org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
                verify(mailService).sendPasswordResetEmail(eq(account.email()), tokenCaptor.capture());
                String token = tokenCaptor.getValue();

                mockMvc.perform(post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetPasswordBody(token, NEW_PASSWORD)))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Senha redefinida com sucesso.")));

                mockMvc.perform(get("/auth/me")
                                .header("Authorization", "Bearer " + oldJwt))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody(account.email(), OLD_PASSWORD)))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody(account.email(), NEW_PASSWORD)))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetPasswordBody(token, "AnotherSecurePassword987!")))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString(
                                                "O token é inválido, expirou ou já foi utilizado.")));
        }

        @Test
        void forgotPasswordDoesNotRevealWhetherEmailExists() throws Exception {
                String unknownEmail = "unknown-" + UUID.randomUUID() + "@example.com";

                mockMvc.perform(post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emailBody(unknownEmail)))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                "Se existir uma conta confirmada para esse email, enviaremos as instruções de redefinição.")));

                verifyNoInteractions(mailService);
        }

        @Test
        void forgotPasswordDoesNotSendEmailForUnverifiedAccount() throws Exception {
                Account account = createAccount(false);

                mockMvc.perform(post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emailBody(account.email())))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                "Se existir uma conta confirmada para esse email, enviaremos as instruções de redefinição.")));

                verifyNoInteractions(mailService);
        }

        @Test
        void resetPasswordRejectsUnknownToken() throws Exception {
                mockMvc.perform(post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetPasswordBody("invalid-token", NEW_PASSWORD)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString(
                                                "O token é inválido, expirou ou já foi utilizado.")));
        }

        private Account createAccount(boolean emailVerified) {
                String unique = UUID.randomUUID().toString();
                String email = "recovery-test-" + unique + "@example.com";
                String username = "recovery-" + unique;

                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode(OLD_PASSWORD));
                user.setEmailVerified(emailVerified);
                User savedUser = userRepository.saveAndFlush(user);

                Profile profile = new Profile();
                profile.setUser(savedUser);
                profile.setUsername(username);
                profile.setDisplayName("Password Recovery Test");
                profileRepository.saveAndFlush(profile);

                return new Account(savedUser.getEmail(), username);
        }

        private String emailBody(String email) {
                return "{\"email\":\"" + email + "\"}";
        }

        private String loginBody(String email, String password) {
                return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        }

        private String resetPasswordBody(String token, String newPassword) {
                return "{\"token\":\"" + token + "\",\"newPassword\":\"" + newPassword + "\"}";
        }

        private String extractAccessToken(String responseBody) {
                Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
                if (!matcher.find()) {
                        throw new AssertionError("A resposta do login deveria conter accessToken.");
                }
                return matcher.group(1);
        }

        private record Account(String email, String username) {
        }
}
