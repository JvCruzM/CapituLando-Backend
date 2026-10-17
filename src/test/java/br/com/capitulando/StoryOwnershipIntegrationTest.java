package br.com.capitulando;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.StoryRepository;
import br.com.capitulando.repository.UserRepository;
import br.com.capitulando.security.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoryOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void listagemDeveRetornarSomenteHistoriasDoUsuarioAutenticado()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Minha historia " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Historia alheia " + UUID.randomUUID());

        mockMvc.perform(get("/stories")
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(ownStory.getTitle())))
                .andExpect(content().string(
                        not(containsString(otherStory.getTitle()))));
    }

    @Test
    void usuarioNaoDeveConsultarHistoriaDeOutroUsuario()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia privada " + UUID.randomUUID());

        mockMvc.perform(get("/stories/{id}", otherStory.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioNaoDeveAlterarHistoriaDeOutroUsuario()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia original " + UUID.randomUUID());

        String originalTitle = otherStory.getTitle();

        mockMvc.perform(put("/stories/{id}", otherStory.getId())
                .header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Tentativa de invasao"
                        }
                        """))
                .andExpect(status().isNotFound());

        Story persistedStory = storyRepository
                .findById(otherStory.getId())
                .orElseThrow();

        assertTrue(
                originalTitle.equals(persistedStory.getTitle()),
                "A história de outro usuário não deve ser alterada.");
    }

    @Test
    void usuarioNaoDeveExcluirHistoriaDeOutroUsuario()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia que deve continuar existindo " + UUID.randomUUID());

        mockMvc.perform(delete("/stories/{id}", otherStory.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());

        assertTrue(
                storyRepository.existsById(otherStory.getId()),
                "A história de outro usuário não deve ser excluída.");
    }

    private TestAccount createAccount() {
        String unique = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("owner-" + unique + "@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("TestPassword123!"));
        user.setEmailVerified(true);

        User savedUser = userRepository.saveAndFlush(user);

        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setUsername("writer-" + unique);
        profile.setDisplayName("Writer " + unique);

        Profile savedProfile = profileRepository.saveAndFlush(profile);

        String token = jwtService.generateAccessToken(savedUser);

        return new TestAccount(savedProfile, token);
    }

    private Story createStory(Profile profile, String title) {
        Instant now = Instant.now();

        Story story = new Story();
        story.setProfile(profile);
        story.setTitle(title);
        story.setCreatedAt(now);
        story.setUpdatedAt(now);
        story.setProgressPercentage(0);

        return storyRepository.saveAndFlush(story);
    }

    private record TestAccount(Profile profile, String token) {
    }
}