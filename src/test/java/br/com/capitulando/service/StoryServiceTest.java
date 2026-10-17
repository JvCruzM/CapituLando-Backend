package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.StoryStatus;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.StoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository repository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private StoryService service;

    @Test
    void save_deveDefinirDatasEAtribuirPerfilAutenticado() {
        UUID profileId = UUID.randomUUID();

        Profile owner = new Profile();
        owner.setId(profileId);

        Profile forgedProfile = new Profile();
        forgedProfile.setId(UUID.randomUUID());

        Story story = new Story();
        story.setTitle("Minha história");
        story.setProfile(forgedProfile);

        when(profileRepository.findById(profileId))
                .thenReturn(Optional.of(owner));

        when(repository.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();

        Story result = service.save(profileId, story);

        Instant after = Instant.now();

        assertSame(story, result);
        assertSame(owner, result.getProfile());
        assertNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(after));
        assertEquals(StoryStatus.DRAFT, result.getStatus());
        assertEquals(0, result.getProgressPercentage());

        verify(profileRepository).findById(profileId);
        verify(repository).save(story);
    }

    @Test
    void findAll_deveRetornarSomenteHistoriasDoPerfilConsultado() {
        UUID profileId = UUID.randomUUID();
        List<Story> expected = List.of(new Story());

        when(repository.findAllByProfileId(profileId))
                .thenReturn(expected);

        List<Story> result = service.findAll(profileId);

        assertEquals(expected, result);
        verify(repository).findAllByProfileId(profileId);
        verify(repository, never()).findAll();
    }

    @Test
    void findById_deveRetornarHistoriaDoPerfilAutorizado() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Story story = new Story();
        story.setId(id);

        when(repository.findByIdAndProfileId(id, profileId))
                .thenReturn(Optional.of(story));

        Story result = service.findById(id, profileId);

        assertSame(story, result);
        verify(repository).findByIdAndProfileId(id, profileId);
    }

    @Test
    void findById_deveRetornar404QuandoHistoriaNaoPertenceAoPerfil() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(repository.findByIdAndProfileId(id, profileId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findById(id, profileId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findByIdAndProfileId(id, profileId);
    }

    @Test
    void update_deveAtualizarHistoriaSemTransferirAutoria() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Profile owner = new Profile();
        owner.setId(profileId);

        Profile forgedProfile = new Profile();
        forgedProfile.setId(UUID.randomUUID());

        Story existing = new Story();
        existing.setId(id);
        existing.setProfile(owner);
        existing.setTitle("Título antigo");

        Story details = new Story();
        details.setTitle("Título atualizado");
        details.setProfile(forgedProfile);

        when(repository.findByIdAndProfileId(id, profileId))
                .thenReturn(Optional.of(existing));

        when(repository.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = service.update(id, profileId, details);

        assertSame(existing, result);
        assertEquals("Título atualizado", result.getTitle());
        assertSame(owner, result.getProfile());
        assertNotNull(result.getUpdatedAt());

        verify(repository).findByIdAndProfileId(id, profileId);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirHistoriaDoPerfilAutorizado() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        Story story = new Story();
        story.setId(id);

        when(repository.findByIdAndProfileId(id, profileId))
                .thenReturn(Optional.of(story));

        service.delete(id, profileId);

        verify(repository).findByIdAndProfileId(id, profileId);
        verify(repository).delete(story);
    }

    @Test
    void delete_naoDeveExcluirHistoriaDeOutroPerfil() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(repository.findByIdAndProfileId(id, profileId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> service.delete(id, profileId));

        verify(repository, never()).delete(any(Story.class));
    }
}