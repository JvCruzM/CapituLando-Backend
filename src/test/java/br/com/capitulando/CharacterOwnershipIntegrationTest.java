package br.com.capitulando;

import br.com.capitulando.model.Character;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.CharacterRepository;
import br.com.capitulando.repository.CharacterRelationshipRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterOwnershipIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProfileRepository profileRepository;

        @Autowired
        private StoryRepository storyRepository;

        @Autowired
        private CharacterRepository characterRepository;

        @Autowired
        private CharacterRelationshipRepository characterRelationshipRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtService jwtService;

        @Test
        void listagemDeveRetornarSomentePersonagensDasPropriasHistorias()
                        throws Exception {

                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                Story ownStory = createStory(
                                owner.profile(),
                                "Historia propria " + UUID.randomUUID());

                Story otherStory = createStory(
                                other.profile(),
                                "Historia alheia " + UUID.randomUUID());

                Character ownCharacter = createCharacter(
                                ownStory,
                                "Meu personagem " + UUID.randomUUID());

                Character otherCharacter = createCharacter(
                                otherStory,
                                "Personagem alheio " + UUID.randomUUID());

                mockMvc.perform(get("/characters")
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isOk())
                                .andExpect(content().string(
                                                containsString(ownCharacter.getName())))
                                .andExpect(content().string(
                                                not(containsString(otherCharacter.getName()))));
        }

        @Test
        void usuarioNaoDeveConsultarPersonagemDeOutraHistoria()
                        throws Exception {

                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                Story otherStory = createStory(
                                other.profile(),
                                "Historia privada " + UUID.randomUUID());

                Character otherCharacter = createCharacter(
                                otherStory,
                                "Personagem privado " + UUID.randomUUID());

                mockMvc.perform(get(
                                "/characters/{id}",
                                otherCharacter.getId())
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void usuarioNaoDeveCriarPersonagemEmHistoriaAlheia()
                        throws Exception {

                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                Story otherStory = createStory(
                                other.profile(),
                                "Historia de outro usuario " + UUID.randomUUID());

                mockMvc.perform(post("/characters")
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
                                characterRepository.findByStoryId(otherStory.getId())
                                                .isEmpty(),
                                "Não deve ser criado personagem na história de outro usuário.");
        }

        @Test
        void usuarioNaoDeveMoverPersonagemParaHistoriaAlheia()
                        throws Exception {

                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                Story ownStory = createStory(
                                owner.profile(),
                                "Historia original " + UUID.randomUUID());

                Story otherStory = createStory(
                                other.profile(),
                                "Historia de destino " + UUID.randomUUID());

                Character ownCharacter = createCharacter(
                                ownStory,
                                "Personagem a proteger " + UUID.randomUUID());

                String originalName = ownCharacter.getName();

                mockMvc.perform(put(
                                "/characters/{id}",
                                ownCharacter.getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "%s",
                                                    "story": {
                                                        "id": "%s"
                                                    }
                                                }
                                                """.formatted(originalName, otherStory.getId())))
                                .andExpect(status().isNotFound());

                Character persistedCharacter = characterRepository
                                .findById(ownCharacter.getId())
                                .orElseThrow();

                assertEquals(originalName, persistedCharacter.getName());
                assertEquals(
                                ownStory.getId(),
                                persistedCharacter.getStory().getId(),
                                "O personagem deve continuar vinculado à história original.");
        }

        @Test
        void usuarioNaoDeveExcluirPersonagemDeOutraHistoria()
                        throws Exception {

                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                Story otherStory = createStory(
                                other.profile(),
                                "Historia a preservar " + UUID.randomUUID());

                Character otherCharacter = createCharacter(
                                otherStory,
                                "Personagem que deve continuar existindo " + UUID.randomUUID());

                mockMvc.perform(delete(
                                "/characters/{id}",
                                otherCharacter.getId())
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isNotFound());

                assertTrue(
                                characterRepository.existsById(otherCharacter.getId()),
                                "O personagem de outro usuário não deve ser excluído.");
        }

        @Test
        void relacionamentosDevemSerPrivadosEExclusaoDePersonagemRemoveVinculos()
                        throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();
                Story ownStory = createStory(owner.profile(), "Historia relacoes " + UUID.randomUUID());
                Story otherStory = createStory(other.profile(), "Historia alheia " + UUID.randomUUID());
                Character source = createCharacter(ownStory, "Origem " + UUID.randomUUID());
                Character target = createCharacter(ownStory, "Destino " + UUID.randomUUID());
                Character foreignCharacter = createCharacter(otherStory, "Alheio " + UUID.randomUUID());

                String response = mockMvc.perform(post(
                                "/stories/{storyId}/character-relationships", ownStory.getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "sourceCharacterId": "%s",
                                                    "targetCharacterId": "%s",
                                                    "type": "amizade",
                                                    "description": "Aliados desde o início"
                                                }
                                                """.formatted(source.getId(), target.getId())))
                                .andExpect(status().isCreated())
                                .andExpect(content().string(containsString("Aliados desde o início")))
                                .andReturn().getResponse().getContentAsString();

                assertTrue(response.contains(source.getId().toString()));
                mockMvc.perform(get("/stories/{storyId}/character-relationships", ownStory.getId())
                                .header("Authorization", "Bearer " + other.token()))
                                .andExpect(status().isNotFound());

                mockMvc.perform(post("/stories/{storyId}/character-relationships", ownStory.getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "sourceCharacterId": "%s",
                                                    "targetCharacterId": "%s",
                                                    "type": "inválido"
                                                }
                                                """.formatted(source.getId(), foreignCharacter.getId())))
                                .andExpect(status().isNotFound());

                mockMvc.perform(delete("/characters/{id}", source.getId())
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isNoContent());

                assertTrue(characterRelationshipRepository
                                .findAllByStory_IdOrderByCreatedAtAsc(ownStory.getId()).isEmpty());
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
                profile.setUsername("char-writer-" + unique);
                profile.setDisplayName("Character Writer " + unique);

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

        private Character createCharacter(Story story, String name) {
                Instant now = Instant.now();

                Character character = new Character();
                character.setStory(story);
                character.setName(name);
                character.setCreatedAt(now);
                character.setUpdatedAt(now);

                return characterRepository.saveAndFlush(character);
        }

        private record TestAccount(Profile profile, String token) {
        }
}
