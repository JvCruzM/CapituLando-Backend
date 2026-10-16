package br.com.capitulando.service;

import br.com.capitulando.model.Story;
import br.com.capitulando.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private StoryService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Story entity = new Story();

        when(repository.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Story result = service.save(entity);
        Instant after = Instant.now();

        assertSame(entity, result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(after));
        assertFalse(result.getUpdatedAt().isBefore(before));
        assertFalse(result.getUpdatedAt().isAfter(after));

        verify(repository).save(entity);
    }

    @Test
    void findAll_deveRetornarResultadosDoRepositorio() {
        Story entity = new Story();
        List<Story> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Story> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Story entity = new Story();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Story result = service.findById(id);

        assertSame(entity, result);
        verify(repository).findById(id);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(id));

        verify(repository).findById(id);
    }

    @Test
    void update_deveAtualizarCampoESalvarEntidade() {
        UUID id = UUID.randomUUID();
        Story existing = new Story();
        existing.setId(id);
        existing.setTitle("Valor antigo");
        Story details = new Story();
        details.setTitle("História atualizada");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("História atualizada", result.getTitle());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Story existing = new Story();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByProfileId_deveDelegarAoRepositorio() {
        UUID profileId = UUID.randomUUID();
        List<Story> expected = List.of(new Story());
        when(repository.findByProfileId(profileId)).thenReturn(expected);

        List<Story> result = service.findByProfileId(profileId);

        assertEquals(expected, result);
        verify(repository).findByProfileId(profileId);
    }

}
