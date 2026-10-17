package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
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
class RaceServiceTest {

    @Mock
    private RaceRepository repository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private RaceService service;

    @Test
    void save_deveDefinirDatasESalvarNaHistoriaDoUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        Profile profile = new Profile();
        profile.setId(profileId);

        Story ownedStory = new Story();
        ownedStory.setId(storyId);
        ownedStory.setProfile(profile);

        Story storyReference = new Story();
        storyReference.setId(storyId);

        Race race = new Race();
        race.setName("Elfos");
        race.setStory(storyReference);

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(ownedStory));

        when(repository.save(any(Race.class)))
                .thenAnswer(invocation -> {
                    Race saved = invocation.getArgument(0);
                    saved.setId(raceId);
                    return saved;
                });

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenAnswer(invocation -> Optional.of(race));

        Instant before = Instant.now();

        Race result = service.save(profileId, race);

        Instant after = Instant.now();

        assertEquals(raceId, result.getId());
        assertSame(ownedStory, result.getStory());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(after));
        assertFalse(result.getUpdatedAt().isBefore(before));
        assertFalse(result.getUpdatedAt().isAfter(after));

        verify(storyRepository).findByIdAndProfileId(storyId, profileId);
        verify(repository).save(race);
        verify(repository).findByIdAndStory_Profile_Id(raceId, profileId);
    }

    @Test
    void save_deveRejeitarHistoriaDeOutroUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();

        Story storyReference = new Story();
        storyReference.setId(storyId);

        Race race = new Race();
        race.setName("Elfos");
        race.setStory(storyReference);

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.save(profileId, race));

        assertEquals(404, exception.getStatusCode().value());
        verify(repository, never()).save(any(Race.class));
    }

    @Test
    void findAll_deveRetornarSomenteRacasDoPerfil() {
        UUID profileId = UUID.randomUUID();
        List<Race> expected = List.of(new Race());

        when(repository.findAllByStory_Profile_Id(profileId))
                .thenReturn(expected);

        List<Race> result = service.findAll(profileId);

        assertEquals(expected, result);
        verify(repository).findAllByStory_Profile_Id(profileId);
    }

    @Test
    void findById_deveRetornarRacaDoPerfil() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        Race race = new Race();
        race.setId(raceId);

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.of(race));

        Race result = service.findById(raceId, profileId);

        assertSame(race, result);
        verify(repository).findByIdAndStory_Profile_Id(raceId, profileId);
    }

    @Test
    void findById_deveLancar404QuandoRacaNaoPertencerAoPerfil() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(raceId, profileId));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void update_deveAtualizarRacaPropria() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        Race existing = new Race();
        existing.setId(raceId);
        existing.setName("Nome antigo");

        Race details = new Race();
        details.setName("Raça atualizada");

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.of(existing));

        when(repository.save(existing)).thenReturn(existing);

        Race result = service.update(raceId, profileId, details);

        assertSame(existing, result);
        assertEquals("Raça atualizada", result.getName());
        assertNotNull(result.getUpdatedAt());

        verify(repository).save(existing);
    }

    @Test
    void update_deveRejeitarMovimentacaoParaHistoriaAlheia() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();
        UUID otherStoryId = UUID.randomUUID();

        Race existing = new Race();
        existing.setId(raceId);
        existing.setName("Nome original");

        Story otherStoryReference = new Story();
        otherStoryReference.setId(otherStoryId);

        Race details = new Race();
        details.setName("Nome alterado");
        details.setStory(otherStoryReference);

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.of(existing));

        when(storyRepository.findByIdAndProfileId(otherStoryId, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(raceId, profileId, details));

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Nome original", existing.getName());

        verify(repository, never()).save(any(Race.class));
    }

    @Test
    void delete_deveExcluirRacaPropria() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        Race existing = new Race();
        existing.setId(raceId);

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.of(existing));

        service.delete(raceId, profileId);

        verify(repository).delete(existing);
    }

    @Test
    void delete_naoDeveExcluirRacaDeOutroUsuario() {
        UUID profileId = UUID.randomUUID();
        UUID raceId = UUID.randomUUID();

        when(repository.findByIdAndStory_Profile_Id(raceId, profileId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> service.delete(raceId, profileId));

        verify(repository, never()).delete(any(Race.class));
    }

    @Test
    void findByStoryId_deveVerificarPropriedadeAntesDeListar() {
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();

        Story story = new Story();
        story.setId(storyId);

        List<Race> expected = List.of(new Race());

        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(story));

        when(repository.findByStoryIdAndStory_Profile_Id(storyId, profileId))
                .thenReturn(expected);

        List<Race> result = service.findByStoryId(storyId, profileId);

        assertEquals(expected, result);

        verify(storyRepository).findByIdAndProfileId(storyId, profileId);
        verify(repository).findByStoryIdAndStory_Profile_Id(storyId, profileId);
    }
}