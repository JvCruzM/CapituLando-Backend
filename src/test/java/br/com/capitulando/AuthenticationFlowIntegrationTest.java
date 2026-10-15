package br.com.capitulando;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    private static final String PASSWORD = "CorrectPassword123!";
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

    @Test
    void loginReturnsJwtForVerifiedAccountAndJwtOpensMeEndpoint() throws Exception {
        Account account = createAccount(true);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(account.email(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"tokenType\":\"Bearer\"")))
                .andExpect(content().string(containsString("\"emailVerified\":true")))
                .andReturn();

        String token = extractAccessToken(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(account.email())))
                .andExpect(content().string(containsString(account.username())))
                .andExpect(content().string(containsString("\"emailVerified\":true")));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        Account account = createAccount(true);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(account.email(), "WrongPassword123!")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Email ou senha inválidos.")));
    }

    @Test
    void loginRejectsAccountWithoutVerifiedEmail() throws Exception {
        Account account = createAccount(false);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(account.email(), PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Confirme seu email antes de entrar.")));
    }

    @Test
    void meEndpointRejectsRequestsWithoutJwt() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private Account createAccount(boolean emailVerified) {
        String unique = UUID.randomUUID().toString();
        String email = "auth-test-" + unique + "@example.com";
        String username = "writer-" + unique;

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setEmailVerified(emailVerified);
        User savedUser = userRepository.saveAndFlush(user);

        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setUsername(username);
        profile.setDisplayName("Integration Test Writer");
        profileRepository.saveAndFlush(profile);

        return new Account(savedUser.getEmail(), username);
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String extractAccessToken(String responseBody) {
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
        assertTrue(matcher.find(), "A resposta do login deveria conter accessToken.");
        return matcher.group(1);
    }

    private record Account(String email, String username) {
    }
}
