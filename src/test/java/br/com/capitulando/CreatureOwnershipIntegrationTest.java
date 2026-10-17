package br.com.capitulando;

import br.com.capitulando.model.Creature;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.CreatureRepository;
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
class CreatureOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private CreatureRepository creatureRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void listagemDeveRetornarSomenteCriaturasDasPropriasHistorias()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Historia propria " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Historia alheia " + UUID.randomUUID());

        Creature ownCreature = createCreature(
                ownStory,
                "Minha criatura " + UUID.randomUUID());

        Creature otherCreature = createCreature(
                otherStory,
                "Criatura alheia " + UUID.randomUUID());

        mockMvc.perform(get("/creatures")
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(ownCreature.getName())))
                .andExpect(content().string(
                        not(containsString(otherCreature.getName()))));
    }

    @Test
    void usuarioNaoDeveConsultarCriaturaDeOutraHistoria()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia privada " + UUID.randomUUID());

        Creature otherCreature = createCreature(
                otherStory,
                "Criatura privada " + UUID.randomUUID());

        mockMvc.perform(get("/creatures/{id}", otherCreature.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioNaoDeveCriarCriaturaEmHistoriaAlheia()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia alheia " + UUID.randomUUID());

        mockMvc.perform(post("/creatures")
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
                creatureRepository.findByStoryId(otherStory.getId()).isEmpty(),
                "Não deve ser criada uma criatura na história de outro usuário.");
    }

    @Test
    void usuarioNaoDeveMoverCriaturaParaHistoriaAlheia()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Historia original " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Historia de destino " + UUID.randomUUID());

        Creature ownCreature = createCreature(
                ownStory,
                "Criatura a proteger " + UUID.randomUUID());

        String originalName = ownCreature.getName();

        mockMvc.perform(put("/creatures/{id}", ownCreature.getId())
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

        Creature persisted = creatureRepository
                .findByIdAndStory_Profile_Id(
                        ownCreature.getId(),
                        owner.profile().getId())
                .orElseThrow();

        assertEquals(originalName, persisted.getName());
        assertEquals(
                ownStory.getId(),
                persisted.getStory().getId(),
                "A criatura deve continuar vinculada à história original.");
    }

    @Test
    void usuarioNaoDeveExcluirCriaturaDeOutraHistoria()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story otherStory = createStory(
                other.profile(),
                "Historia a preservar " + UUID.randomUUID());

        Creature otherCreature = createCreature(
                otherStory,
                "Criatura que deve continuar existindo " + UUID.randomUUID());

        mockMvc.perform(delete("/creatures/{id}", otherCreature.getId())
                .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isNotFound());

        assertTrue(
                creatureRepository.existsById(otherCreature.getId()),
                "A criatura de outro usuário não deve ser excluída.");
    }

    @Test
    void usuarioNaoDeveAssociarCriaturaARacaDeOutraHistoria()
            throws Exception {

        TestAccount owner = createAccount();
        TestAccount other = createAccount();

        Story ownStory = createStory(
                owner.profile(),
                "Historia propria " + UUID.randomUUID());

        Story otherStory = createStory(
                other.profile(),
                "Outra historia " + UUID.randomUUID());

        Race otherRace = createRace(
                otherStory,
                "Raça privada " + UUID.randomUUID());

        mockMvc.perform(post("/creatures")
                .header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Criatura de teste",
                            "story": {
                                "id": "%s"
                            },
                            "race": {
                                "id": "%s"
                            }
                        }
                        """.formatted(ownStory.getId(), otherRace.getId())))
                .andExpect(status().isNotFound());

        assertTrue(
                creatureRepository.findByStoryId(ownStory.getId()).isEmpty(),
                "Não deve ser criada uma criatura com raça de outra história.");
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
        profile.setUsername("cr-writer-" + unique);
        profile.setDisplayName("Creature Writer " + unique);

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

    private Creature createCreature(Story story, String name) {
        Instant now = Instant.now();

        Creature creature = new Creature();
        creature.setStory(story);
        creature.setName(name);
        creature.setCreatedAt(now);
        creature.setUpdatedAt(now);

        return creatureRepository.saveAndFlush(creature);
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