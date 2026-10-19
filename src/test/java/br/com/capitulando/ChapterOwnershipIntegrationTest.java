package br.com.capitulando;

import br.com.capitulando.model.Chapter;
import br.com.capitulando.model.ChapterStatus;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.StoryStatus;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ChapterRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChapterOwnershipIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private ProfileRepository profileRepository;
        @Autowired
        private StoryRepository storyRepository;
        @Autowired
        private ChapterRepository chapterRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtService jwtService;

        @Test
        void listagemRetornaSomenteCapitulosDaHistoriaDoProprietario() throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();
                Story ownStory = createStory(owner.profile());
                Story otherStory = createStory(other.profile());
                Chapter ownChapter = createChapter(ownStory, "Capítulo próprio " + UUID.randomUUID());
                Chapter otherChapter = createChapter(otherStory, "Capítulo alheio " + UUID.randomUUID());

                mockMvc.perform(get("/stories/{storyId}/chapters", ownStory.getId())
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(ownChapter.getTitle())))
                                .andExpect(content().string(not(containsString(otherChapter.getTitle()))));

                mockMvc.perform(get("/stories/{storyId}/chapters", otherStory.getId())
                                .header("Authorization", "Bearer " + owner.token()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void usuarioPodeCriarCapituloNaPropriaHistoriaComContagemAutomatica() throws Exception {
                TestAccount owner = createAccount();
                Story story = createStory(owner.profile());

                mockMvc.perform(post("/stories/{storyId}/chapters", story.getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Primeiro capítulo","content":"Uma história começa aqui."}
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.storyId").value(story.getId().toString()))
                                .andExpect(jsonPath("$.orderIndex").value(1))
                                .andExpect(jsonPath("$.wordCount").value(4))
                                .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        void usuarioNaoPodeCriarCapituloEmHistoriaAlheia() throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();
                Story otherStory = createStory(other.profile());

                mockMvc.perform(post("/stories/{storyId}/chapters", otherStory.getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Tentativa de invasão","content":"Conteúdo privado."}
                                                """))
                                .andExpect(status().isNotFound());

                assertTrue(chapterRepository.findByStoryIdAndStoryProfileIdOrderByOrderIndexAsc(
                                otherStory.getId(), other.profile().getId()).isEmpty());
        }

        @Test
        void usuarioNaoPodeConsultarAlterarOuExcluirCapituloAlheio() throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();
                Story otherStory = createStory(other.profile());
                Chapter otherChapter = createChapter(otherStory, "Capítulo que deve permanecer " + UUID.randomUUID());
                String authorization = "Bearer " + owner.token();

                mockMvc.perform(get("/stories/{storyId}/chapters/{chapterId}", otherStory.getId(), otherChapter.getId())
                                .header("Authorization", authorization))
                                .andExpect(status().isNotFound());

                mockMvc.perform(put("/stories/{storyId}/chapters/{chapterId}", otherStory.getId(), otherChapter.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"title":"Tentativa de alteração","content":"Conteúdo alterado."}
                                                """))
                                .andExpect(status().isNotFound());

                mockMvc.perform(delete("/stories/{storyId}/chapters/{chapterId}", otherStory.getId(),
                                otherChapter.getId())
                                .header("Authorization", authorization))
                                .andExpect(status().isNotFound());

                Chapter persisted = chapterRepository.findById(otherChapter.getId()).orElseThrow();
                assertEquals(otherChapter.getTitle(), persisted.getTitle());
                assertTrue(chapterRepository.existsById(otherChapter.getId()));
        }

        private TestAccount createAccount() {
                String unique = UUID.randomUUID().toString();
                User user = new User();
                user.setEmail("chapter-owner-" + unique + "@example.com");
                user.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
                user.setEmailVerified(true);
                User savedUser = userRepository.saveAndFlush(user);

                Profile profile = new Profile();
                profile.setUser(savedUser);
                profile.setUsername("chapter-writer-" + unique.substring(0, 32));
                profile.setDisplayName("Chapter Writer " + unique);
                Profile savedProfile = profileRepository.saveAndFlush(profile);

                return new TestAccount(savedProfile, jwtService.generateAccessToken(savedUser));
        }

        private Story createStory(Profile profile) {
                Instant now = Instant.now();
                Story story = new Story();
                story.setProfile(profile);
                story.setTitle("História " + UUID.randomUUID());
                story.setCreatedAt(now);
                story.setUpdatedAt(now);
                story.setStatus(StoryStatus.DRAFT);
                story.setProgressPercentage(0);
                return storyRepository.saveAndFlush(story);
        }

        private Chapter createChapter(Story story, String title) {
                Instant now = Instant.now();
                Chapter chapter = new Chapter();
                chapter.setStory(story);
                chapter.setOrderIndex(1);
                chapter.setTitle(title);
                chapter.setContent("Conteúdo de teste.");
                chapter.setStatus(ChapterStatus.DRAFT);
                chapter.setWordCount(3);
                chapter.setCreatedAt(now);
                chapter.setUpdatedAt(now);
                return chapterRepository.saveAndFlush(chapter);
        }

        private record TestAccount(Profile profile, String token) {
        }
}
