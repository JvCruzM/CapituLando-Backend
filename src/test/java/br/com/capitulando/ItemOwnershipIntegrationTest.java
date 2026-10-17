package br.com.capitulando;

import br.com.capitulando.model.Item;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ItemRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void listagemDeveRetornarSomenteItensDasPropriasHistorias() throws Exception {
        TestAccount owner = createAccount();
        TestAccount other = createAccount();
        Item own = createItem(createStory(owner.profile()), "Item proprio " + UUID.randomUUID());
        Item otherItem = createItem(createStory(other.profile()), "Item alheio " + UUID.randomUUID());

        mockMvc.perform(get("/items").header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(own.getName())))
                .andExpect(content().string(not(containsString(otherItem.getName()))));
    }

    @Test
    void usuarioNaoDeveCriarOuMoverItemParaHistoriaAlheia() throws Exception {
        TestAccount owner = createAccount();
        TestAccount other = createAccount();
        Story ownStory = createStory(owner.profile());
        Story otherStory = createStory(other.profile());

        mockMvc.perform(post("/items").header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Tentativa","story":{"id":"%s"}}
                        """.formatted(otherStory.getId())))
                .andExpect(status().isNotFound());
        assertTrue(
                itemRepository.findByStoryIdAndStory_Profile_Id(otherStory.getId(), other.profile().getId()).isEmpty());

        Item ownItem = createItem(ownStory, "Item protegido " + UUID.randomUUID());
        mockMvc.perform(put("/items/{id}", ownItem.getId()).header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Alterado","story":{"id":"%s"}}
                        """.formatted(otherStory.getId())))
                .andExpect(status().isNotFound());
        Item persisted = itemRepository.findByIdAndStory_Profile_Id(ownItem.getId(), owner.profile().getId())
                .orElseThrow();
        assertEquals(ownItem.getName(), persisted.getName());
        assertEquals(ownStory.getId(), persisted.getStory().getId());
    }

    @Test
    void usuarioNaoDeveConsultarOuExcluirItemDeOutroUsuario() throws Exception {
        TestAccount owner = createAccount();
        TestAccount other = createAccount();
        Item item = createItem(createStory(other.profile()), "Item privado " + UUID.randomUUID());
        String authorization = "Bearer " + owner.token();

        mockMvc.perform(get("/items/{id}", item.getId()).header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/items/{id}", item.getId()).header("Authorization", authorization))
                .andExpect(status().isNotFound());
        assertTrue(itemRepository.existsById(item.getId()));
    }

    private TestAccount createAccount() {
        String unique = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail("item-owner-" + unique + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
        user.setEmailVerified(true);
        User savedUser = userRepository.saveAndFlush(user);
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setUsername("item-writer-" + unique);
        profile.setDisplayName("Item Writer " + unique);
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
        story.setProgressPercentage(0);
        return storyRepository.saveAndFlush(story);
    }

    private Item createItem(Story story, String name) {
        Instant now = Instant.now();
        Item item = new Item();
        item.setStory(story);
        item.setName(name);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return itemRepository.saveAndFlush(item);
    }

    private record TestAccount(Profile profile, String token) {
    }
}
