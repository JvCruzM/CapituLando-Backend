package br.com.capitulando;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.RaceRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RaceOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void listagemDeveRetornarSomenteRacasDasPropriasHistorias()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Historia propria " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Historia alheia " + UUID.randomUUID());

        Race ownRace = createRace(
                ownStory,
                "Minha raça " + UUID.randomUUID());

        Race otherRace = createRace(
                otherStory,
                "Raça alheia " + UUID.randomUUID());

        mockMvc.perform(get("/races")
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(ownRace.getName())))
                .andExpect(content().string(
                        not(containsString(otherRace.getName()))));
    }

    @Test
    void usuarioNaoDeveConsultarRacaDeOutraHistoria()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia privada " + UUID.randomUUID());

        Race otherRace = createRace(
                otherStory,
                "Raça privada " + UUID.randomUUID());

        mockMvc.perform(get("/races/{id}", otherRace.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioNaoDeveCriarRacaEmHistoriaAlheia()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia de outro usuario " + UUID.randomUUID());

        mockMvc.perform(post("/races")
                .header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Tentativa de invasao",
                            "story": {
                                "id": "%s"
                            }
                        }
                        """.formatted(otherStory.getId())))
                .andExpect(status().isNotFound());

        assertTrue(
                raceRepository.findByStoryId(otherStory.getId()).isEmpty(),
                "Não deve ser criada uma raça na história de outro usuário.");
    }

    @Test
    void usuarioNaoDeveMoverRacaParaHistoriaAlheia()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Historia original " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Historia de destino " + UUID.randomUUID());

        Race ownRace = createRace(
                ownStory,
                "Raça a proteger " + UUID.randomUUID());

        String originalName = ownRace.getName();

        mockMvc.perform(put("/races/{id}", ownRace.getId())
                .header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Tentativa de alterar",
                            "story": {
                                "id": "%s"
                            }
                        }
                        """.formatted(otherStory.getId())))
                .andExpect(status().isNotFound());

        Race persistedRace = raceRepository
                .findByIdAndStory_Profile_Id(
                        ownRace.getId(),
                        owner.profile().getId())
                .orElseThrow();

        assertEquals(originalName, persistedRace.getName());
        assertEquals(
                ownStory.getId(),
                persistedRace.getStory().getId(),
                "A raça deve continuar vinculada à história original.");
    }

    @Test
    void usuarioNaoDeveExcluirRacaDeOutraHistoria()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia a preservar " + UUID.randomUUID());

        Race otherRace = createRace(
                otherStory,
                "Raça que deve continuar existindo " + UUID.randomUUID());

        mockMvc.perform(delete("/races/{id}", otherRace.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());

        assertTrue(
                raceRepository.existsById(otherRace.getId()),
                "A raça de outro usuário não deve ser excluída.");
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
        profile.setUsername("race-writer-" + unique);
        profile.setDisplayName("Race Writer " + unique);

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

    private Race createRace(Story story, String name) {
        Instant now = Instant.now();

        Race race = new Race();
        race.setStory(story);
        race.setName(name);
        race.setCreatedAt(now);
        race.setUpdatedAt(now);

        return raceRepository.saveAndFlush(race);
    }

    private record TestAccount(Profile profile, String token) {
    }
}