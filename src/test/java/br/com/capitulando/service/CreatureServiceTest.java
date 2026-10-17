package br.com.capitulando.service;

import br.com.capitulando.model.Creature;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CreatureRepository;
import br.com.capitulando.repository.RaceRepository;
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
class CreatureServiceTest {

    @Mock
    private CreatureRepository repository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private RaceRepository raceRepository;

    @InjectMocks
    private CreatureService service;

    @Test
    void save_deveDefinirDatasESalvarNaHistoriaDoUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        Story ownedStory = new Story();
        ownedStory.setId(storyId);
        ownedStory.setProfile(profile);

        Story storyReference = new Story();
        storyReference.setId(storyId);

        Creature creature = new Creature();
        creature.setName("Dragão");
        creature.setStory(storyReference);

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(ownedStory));

        when(repository.save(any(Creature.class)))
                .thenAnswer(invocation -> {
                    Creature saved = invocation.getArgument(0);
                    saved.setId(creatureId);
                    return saved;
                });

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenAnswer(invocation -> Optional.of(creature));

        Instant before = Instant.now();
        Creature result = service.save(profileId, creature);
        Instant after = Instant.now();

        assertEquals(creatureId, result.getId());
        assertSame(ownedStory, result.getStory());
        assertNull(result.getRace());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(after));
        assertFalse(result.getUpdatedAt().isBefore(before));
        assertFalse(result.getUpdatedAt().isAfter(after));

        verify(repository).save(creature);
        verify(repository).findByIdAndStory_Profile_Id(
                creatureId, profileId);
    }

    @Test
    void save_deveRejeitarHistoriaDeOutroUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();

        Story reference = new Story();
        reference.setId(storyId);

        Creature creature = new Creature();
        creature.setName("Criatura");
        creature.setStory(reference);

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.save(profileId, creature));

        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).save(any(Creature.class));
    }

    @Test
    void save_deveRejeitarRacaDeOutraHistoria() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        Story ownedStory = new Story();
        ownedStory.setId(storyId);

        Story storyReference = new Story();
        storyReference.setId(storyId);

        Race raceReference = new Race();
        raceReference.setId(raceId);

        Creature creature = new Creature();
        creature.setStory(storyReference);
        creature.setRace(raceReference);

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(ownedStory));

        when(raceRepository.findByIdAndStory_IdAndStory_Profile_Id(
                raceId, storyId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.save(profileId, creature));

        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).save(any(Creature.class));
    }

    @Test
    void findAll_deveRetornarCriaturasDoPerfil() {
        UUID profileId = UUID.randomUUID();
        List<Creature> expected = List.of(new Creature());

        when(repository.findAllByStory_Profile_Id(profileId))
                .thenReturn(expected);

        List<Creature> result = service.findAll(profileId);

        assertEquals(expected, result);
        verify(repository).findAllByStory_Profile_Id(profileId);
    }

    @Test
    void findById_deveRetornarCriaturaDoPerfil() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        Creature creature = new Creature();
        creature.setId(creatureId);

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.of(creature));

        Creature result = service.findById(creatureId, profileId);

        assertSame(creature, result);
    }

    @Test
    void findById_deveLancar404QuandoCriaturaNaoPertencerAoPerfil() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(creatureId, profileId));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void update_deveAtualizarCriaturaPropria() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        Story story = new Story();
        story.setId(UUID.randomUUID());

        Creature existing = new Creature();
        existing.setId(creatureId);
        existing.setStory(story);
        existing.setName("Nome antigo");

        Creature details = new Creature();
        details.setName("Criatura atualizada");

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.of(existing));

        when(repository.save(existing)).thenReturn(existing);

        Creature result = service.update(creatureId, profileId, details);

        assertSame(existing, result);
        assertEquals("Criatura atualizada", result.getName());
        assertNotNull(result.getUpdatedAt());

        verify(repository).save(existing);
    }

    @Test
    void update_deveRejeitarMovimentacaoParaHistoriaAlheia() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();
        UUID otherStoryId = UUID.randomUUID();

        Story ownStory = new Story();
        ownStory.setId(UUID.randomUUID());

        Story otherStoryReference = new Story();
        otherStoryReference.setId(otherStoryId);

        Creature existing = new Creature();
        existing.setId(creatureId);
        existing.setStory(ownStory);
        existing.setName("Nome original");

        Creature details = new Creature();
        details.setName("Nome alterado");
        details.setStory(otherStoryReference);

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.of(existing));

        when(storyRepository.findByIdAndProfileId(
                otherStoryId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(creatureId, profileId, details));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Nome original", existing.getName());

        verify(repository, never()).save(any(Creature.class));
    }

    @Test
    void delete_deveExcluirCriaturaPropria() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        Creature existing = new Creature();
        existing.setId(creatureId);

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.of(existing));

        service.delete(creatureId, profileId);

        verify(repository).delete(existing);
    }

    @Test
    void delete_naoDeveExcluirCriaturaDeOutroUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        when(repository.findByIdAndStory_Profile_Id(
                creatureId, profileId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> service.delete(creatureId, profileId));

        verify(repository, never()).delete(any(Creature.class));
    }

    @Test
    void findByStoryId_deveVerificarPropriedadeAntesDeListar() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();

        Story story = new Story();
        story.setId(storyId);

        List<Creature> expected = List.of(new Creature());

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(story));

        when(repository.findByStoryIdAndStory_Profile_Id(
                storyId, profileId))
                .thenReturn(expected);

        List<Creature> result = service.findByStoryId(storyId, profileId);

        assertEquals(expected, result);
        verify(repository).findByStoryIdAndStory_Profile_Id(
                storyId, profileId);
    }
}