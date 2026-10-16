package br.com.capitulando.service;

import br.com.capitulando.model.Organization;
import br.com.capitulando.repository.OrganizationRepository;
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
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository repository;

    @InjectMocks
    private OrganizationService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Organization entity = new Organization();

        when(repository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Organization result = service.save(entity);
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
        Organization entity = new Organization();
        List<Organization> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<Organization> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Organization entity = new Organization();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        Organization result = service.findById(id);

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
        Organization existing = new Organization();
        existing.setId(id);
        existing.setName("Valor antigo");
        Organization details = new Organization();
        details.setName("Organização atualizada");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Organization result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Organização atualizada", result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Organization existing = new Organization();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Organization> expected = List.of(new Organization());
        when(repository.findByStoryId(storyId)).thenReturn(expected);

        List<Organization> result = service.findByStoryId(storyId);

        assertEquals(expected, result);
        verify(repository).findByStoryId(storyId);
    }

}
