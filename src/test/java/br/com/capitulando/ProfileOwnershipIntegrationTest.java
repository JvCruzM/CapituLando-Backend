package br.com.capitulando;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileOwnershipIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private ProfileRepository profileRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtService jwtService;

        @Test
        void usuarioNaoDeveCriarPerfilNoLugarDeOutraConta() throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();

                mockMvc.perform(post("/profiles")
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"id":"%s","username":"invasor","displayName":"Invasor"}
                                                """.formatted(other.profile().getId())))
                                .andExpect(status().isConflict());

                Profile unchanged = profileRepository.findById(other.profile().getId()).orElseThrow();
                assertEquals(other.profile().getUsername(), unchanged.getUsername());
                assertEquals(other.profile().getDisplayName(), unchanged.getDisplayName());
        }

        @Test
        void usuarioNaoDeveAlterarOuExcluirPerfilDeOutraConta() throws Exception {
                TestAccount owner = createAccount();
                TestAccount other = createAccount();
                String authorization = "Bearer " + owner.token();

                mockMvc.perform(put("/profiles/{id}", other.profile().getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"username":"alterado","displayName":"Alterado","bio":"Invadido"}
                                                """))
                                .andExpect(status().isNotFound());

                mockMvc.perform(delete("/profiles/{id}", other.profile().getId())
                                .header("Authorization", authorization))
                                .andExpect(status().isNotFound());

                Profile unchanged = profileRepository.findById(other.profile().getId()).orElseThrow();
                assertEquals(other.profile().getUsername(), unchanged.getUsername());
                assertEquals(other.profile().getDisplayName(), unchanged.getDisplayName());
                assertTrue(profileRepository.existsById(other.profile().getId()));
        }

        @Test
        void usuarioPodeAtualizarProprioPerfilSemTrocarIdentidade() throws Exception {
                TestAccount owner = createAccount();

                mockMvc.perform(put("/profiles/{id}", owner.profile().getId())
                                .header("Authorization", "Bearer " + owner.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"id":"%s","username":"novo-nome","displayName":"Nome novo","bio":"Bio nova"}
                                                """
                                                .formatted(UUID.randomUUID())))
                                .andExpect(status().isOk());

                Profile updated = profileRepository.findById(owner.profile().getId()).orElseThrow();
                assertEquals("novo-nome", updated.getUsername());
                assertEquals("Nome novo", updated.getDisplayName());
                assertEquals("Bio nova", updated.getBio());
                assertEquals(owner.profile().getId(), updated.getUser().getId());
        }

        private TestAccount createAccount() {
                String unique = UUID.randomUUID().toString();
                User user = new User();
                user.setEmail("profile-owner-" + unique + "@example.com");
                user.setPasswordHash(passwordEncoder.encode("TestPassword123!"));
                user.setEmailVerified(true);
                User savedUser = userRepository.saveAndFlush(user);

                Profile profile = new Profile();
                profile.setUser(savedUser);
                profile.setUsername("p-" + unique);
                profile.setDisplayName("Profile Writer " + unique);
                Profile savedProfile = profileRepository.saveAndFlush(profile);

                return new TestAccount(savedProfile, jwtService.generateAccessToken(savedUser));
        }

        private record TestAccount(Profile profile, String token) {
        }
}
