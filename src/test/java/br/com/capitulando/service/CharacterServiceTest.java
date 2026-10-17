package br.com.capitulando.service;

import br.com.capitulando.model.Character;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CharacterRepository;
import br.com.capitulando.repository.StoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

        @Mock
        private CharacterRepository repository;

        @Mock
        private StoryRepository storyRepository;

        @InjectMocks
        private CharacterService service;

        @Test
        void save_deveDefinirDatasESalvarPersonagemNaHistoriaDoUsuario() {
                UUID profileId = UUID.randomUUID();
                UUID storyId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                Profile profile = new Profile();
                profile.setId(profileId);

                Story ownedStory = new Story();
                ownedStory.setId(storyId);
                ownedStory.setProfile(profile);

                Story storyReference = new Story();
                storyReference.setId(storyId);

                Character character = new Character();
                character.setName("Personagem de teste");
                character.setStory(storyReference);

                when(storyRepository.findByIdAndProfileId(storyId, profileId))
                                .thenReturn(Optional.of(ownedStory));

                when(repository.save(any(Character.class)))
                                .thenAnswer(invocation -> {
                                        Character saved = invocation.getArgument(0);
                                        saved.setId(characterId);
                                        return saved;
                                });

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenAnswer(invocation -> Optional.of(character));

                Instant before = Instant.now();

                Character result = service.save(profileId, character);

                Instant after = Instant.now();

                assertEquals(characterId, result.getId());
                assertSame(ownedStory, result.getStory());
                assertNotNull(result.getCreatedAt());
                assertNotNull(result.getUpdatedAt());
                assertFalse(result.getCreatedAt().isBefore(before));
                assertFalse(result.getCreatedAt().isAfter(after));
                assertFalse(result.getUpdatedAt().isBefore(before));
                assertFalse(result.getUpdatedAt().isAfter(after));

                verify(storyRepository).findByIdAndProfileId(storyId, profileId);
                verify(repository).save(character);
                verify(repository).findByIdAndStory_Profile_Id(characterId, profileId);
        }

        @Test
        void save_deveRejeitarHistoriaDeOutroUsuario() {
                UUID profileId = UUID.randomUUID();
                UUID storyId = UUID.randomUUID();

                Story storyReference = new Story();
                storyReference.setId(storyId);

                Character character = new Character();
                character.setName("Personagem");
                character.setStory(storyReference);

                when(storyRepository.findByIdAndProfileId(storyId, profileId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ResponseStatusException.class,
                                () -> service.save(profileId, character));

                verify(repository, never()).save(any(Character.class));
        }

        @Test
        void findAll_deveRetornarPersonagensDasHistoriasDoPerfil() {
                UUID profileId = UUID.randomUUID();

                List<Character> expected = List.of(new Character());

                when(repository.findAllByStory_Profile_Id(profileId))
                                .thenReturn(expected);

                List<Character> result = service.findAll(profileId);

                assertEquals(expected, result);
                verify(repository).findAllByStory_Profile_Id(profileId);
        }

        @Test
        void findById_deveRetornarPersonagemQuandoPertencerAoPerfil() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                Character character = new Character();
                character.setId(characterId);

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.of(character));

                Character result = service.findById(characterId, profileId);

                assertSame(character, result);

                verify(repository).findByIdAndStory_Profile_Id(
                                characterId,
                                profileId);
        }

        @Test
        void findById_deveLancar404QuandoPersonagemNaoPertencerAoPerfil() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.findById(characterId, profileId));

                assertEquals(404, exception.getStatusCode().value());
        }

        @Test
        void update_deveAlterarPersonagemProprio() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                Character existing = new Character();
                existing.setId(characterId);
                existing.setName("Nome antigo");

                Character details = new Character();
                details.setName("Nome atualizado");

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.of(existing));

                when(repository.save(existing)).thenReturn(existing);

                Character result = service.update(characterId, profileId, details);

                assertSame(existing, result);
                assertEquals("Nome atualizado", result.getName());
                assertNotNull(result.getUpdatedAt());

                verify(repository).save(existing);
        }

        @Test
        void update_deveRejeitarMovimentacaoParaHistoriaDeOutroUsuario() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();
                UUID otherStoryId = UUID.randomUUID();

                Story otherStoryReference = new Story();
                otherStoryReference.setId(otherStoryId);

                Character existing = new Character();
                existing.setId(characterId);
                existing.setName("Personagem original");

                Character details = new Character();
                details.setName("Personagem alterado");
                details.setStory(otherStoryReference);

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.of(existing));

                when(storyRepository.findByIdAndProfileId(otherStoryId, profileId))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.update(characterId, profileId, details));

                assertEquals(404, exception.getStatusCode().value());
                assertEquals("Personagem original", existing.getName());

                verify(repository, never()).save(any(Character.class));
        }

        @Test
        void delete_deveExcluirPersonagemProprio() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                Character existing = new Character();
                existing.setId(characterId);

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.of(existing));

                service.delete(characterId, profileId);

                verify(repository).delete(existing);
        }

        @Test
        void delete_naoDeveExcluirPersonagemDeOutroUsuario() {
                UUID profileId = UUID.randomUUID();
                UUID characterId = UUID.randomUUID();

                when(repository.findByIdAndStory_Profile_Id(characterId, profileId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ResponseStatusException.class,
                                () -> service.delete(characterId, profileId));

                verify(repository, never()).delete(any(Character.class));
        }
}