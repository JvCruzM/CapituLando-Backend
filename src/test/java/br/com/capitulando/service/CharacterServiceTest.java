package br.com.capitulando.service;

import br.com.capitulando.model.Character;
import br.com.capitulando.repository.CharacterRepository;
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
class CharacterServiceTest {

    @Mock
    private CharacterRepository repository;

    @InjectMocks
    private CharacterService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Character entity = new Character();

        when(repository.save(any(Character.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Character result = service.save(entity);
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
        Character entity = new Character();
        List<Character> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Character> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Character entity = new Character();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Character result = service.findById(id);

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
        Character existing = new Character();
        existing.setId(id);
        existing.setName("Valor antigo");
        Character details = new Character();
        details.setName("Personagem atualizado");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Character.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Character result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Personagem atualizado", result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Character existing = new Character();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Character> expected = List.of(new Character());
        when(repository.findByStoryId(storyId)).thenReturn(expected);

        List<Character> result = service.findByStoryId(storyId);

        assertEquals(expected, result);
        verify(repository).findByStoryId(storyId);
    }

}
