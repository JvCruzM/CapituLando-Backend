package br.com.capitulando.service;

import br.com.capitulando.model.Location;
import br.com.capitulando.repository.LocationRepository;
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
class LocationServiceTest {

    @Mock
    private LocationRepository repository;

    @InjectMocks
    private LocationService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Location entity = new Location();

        when(repository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Location result = service.save(entity);
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
        Location entity = new Location();
        List<Location> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Location> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Location entity = new Location();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Location result = service.findById(id);

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
        Location existing = new Location();
        existing.setId(id);
        existing.setName("Valor antigo");
        Location details = new Location();
        details.setName("Localização atualizada");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Location.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Location result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Localização atualizada", result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Location existing = new Location();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Location> expected = List.of(new Location());
        when(repository.findByStoryId(storyId)).thenReturn(expected);

        List<Location> result = service.findByStoryId(storyId);

        assertEquals(expected, result);
        verify(repository).findByStoryId(storyId);
    }

    @Test
    void findByParentLocationId_deveDelegarAoRepositorio() {
        UUID parentLocationId = UUID.randomUUID();
        List<Location> expected = List.of(new Location());
        when(repository.findByParentLocationId(parentLocationId)).thenReturn(expected);

        List<Location> result = service.findByParentLocationId(parentLocationId);

        assertEquals(expected, result);
        verify(repository).findByParentLocationId(parentLocationId);
    }

}
