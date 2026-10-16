package br.com.capitulando.service;

import br.com.capitulando.model.Chapter;
import br.com.capitulando.repository.ChapterRepository;
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
class ChapterServiceTest {

    @Mock
    private ChapterRepository repository;

    @InjectMocks
    private ChapterService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Chapter entity = new Chapter();
        entity.setContent("uma história com quatro palavras");
        when(repository.save(any(Chapter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Chapter result = service.save(entity);
        Instant after = Instant.now();

        assertSame(entity, result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertFalse(result.getCreatedAt().isBefore(before));
        assertFalse(result.getCreatedAt().isAfter(after));
        assertFalse(result.getUpdatedAt().isBefore(before));
        assertFalse(result.getUpdatedAt().isAfter(after));
        assertEquals(5, result.getWordCount());
        verify(repository).save(entity);
    }

    @Test
    void findAll_deveRetornarResultadosDoRepositorio() {
        Chapter entity = new Chapter();
        List<Chapter> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Chapter> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Chapter entity = new Chapter();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Chapter result = service.findById(id);

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
        Chapter existing = new Chapter();
        existing.setId(id);
        existing.setTitle("Valor antigo");
        Chapter details = new Chapter();
        details.setTitle("Capítulo atualizado");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Chapter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Chapter result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Capítulo atualizado", result.getTitle());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Chapter existing = new Chapter();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Chapter> expected = List.of(new Chapter());
        when(repository.findByStoryIdOrderByOrderIndexAsc(storyId)).thenReturn(expected);

        List<Chapter> result = service.findByStoryId(storyId);

        assertEquals(expected, result);
        verify(repository).findByStoryIdOrderByOrderIndexAsc(storyId);
    }

    @Test
    void save_deveContarPalavrasIgnorandoEspacosExtras() {
        Chapter chapter = new Chapter();
        chapter.setContent("  uma   história\ncom palavras  ");
        when(repository.save(any(Chapter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Chapter result = service.save(chapter);

        assertEquals(4, result.getWordCount());
    }

    @Test
    void save_deveContarZeroPalavrasQuandoConteudoEstiverVazio() {
        Chapter chapter = new Chapter();
        chapter.setContent("   ");
        when(repository.save(any(Chapter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Chapter result = service.save(chapter);

        assertEquals(0, result.getWordCount());
    }

}
