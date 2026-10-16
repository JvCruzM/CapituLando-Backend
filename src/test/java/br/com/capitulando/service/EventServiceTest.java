package br.com.capitulando.service;

import br.com.capitulando.model.Event;
import br.com.capitulando.repository.EventRepository;
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
class EventServiceTest {

    @Mock
    private EventRepository repository;

    @InjectMocks
    private EventService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Event entity = new Event();

        when(repository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Event result = service.save(entity);
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
        Event entity = new Event();
        List<Event> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Event> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Event entity = new Event();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Event result = service.findById(id);

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
        Event existing = new Event();
        existing.setId(id);
        existing.setTitle("Valor antigo");
        Event details = new Event();
        details.setTitle("Evento atualizado");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Event result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Evento atualizado", result.getTitle());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Event existing = new Event();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Event> expected = List.of(new Event());
        when(repository.findByStoryIdOrderBySequenceOrderAsc(storyId)).thenReturn(expected);

        List<Event> result = service.findByStoryId(storyId);

        assertEquals(expected, result);
        verify(repository).findByStoryIdOrderBySequenceOrderAsc(storyId);
    }

    @Test
    void findByChapterId_deveDelegarAoRepositorio() {
        UUID chapterId = UUID.randomUUID();
        List<Event> expected = List.of(new Event());
        when(repository.findByChapterId(chapterId)).thenReturn(expected);

        List<Event> result = service.findByChapterId(chapterId);

        assertEquals(expected, result);
        verify(repository).findByChapterId(chapterId);
    }

}
