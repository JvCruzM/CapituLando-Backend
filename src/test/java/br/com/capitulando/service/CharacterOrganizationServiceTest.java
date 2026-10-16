package br.com.capitulando.service;

import br.com.capitulando.model.CharacterOrganization;
import br.com.capitulando.repository.CharacterOrganizationRepository;
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
class CharacterOrganizationServiceTest {

    @Mock
    private CharacterOrganizationRepository repository;

    @InjectMocks
    private CharacterOrganizationService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        CharacterOrganization entity = new CharacterOrganization();

        when(repository.save(any(CharacterOrganization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        CharacterOrganization result = service.save(entity);
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
        CharacterOrganization entity = new CharacterOrganization();
        List<CharacterOrganization> expected = List.of(entity);
        when(repository.findAll()).thenReturn(expected);

        List<CharacterOrganization> result = service.findAll();

        assertEquals(expected, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        CharacterOrganization entity = new CharacterOrganization();
        entity.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        CharacterOrganization result = service.findById(id);

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
        CharacterOrganization existing = new CharacterOrganization();
        existing.setId(id);
        existing.setRole("Valor antigo");
        CharacterOrganization details = new CharacterOrganization();
        details.setRole("Líder");
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(CharacterOrganization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CharacterOrganization result = service.update(id, details);

        assertSame(existing, result);
        assertEquals("Líder", result.getRole());
        assertNotNull(result.getUpdatedAt());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        CharacterOrganization existing = new CharacterOrganization();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void findByCharacterId_deveDelegarAoRepositorio() {
        UUID characterId = UUID.randomUUID();
        List<CharacterOrganization> expected = List.of(new CharacterOrganization());
        when(repository.findByCharacterId(characterId)).thenReturn(expected);

        List<CharacterOrganization> result = service.findByCharacterId(characterId);

        assertEquals(expected, result);
        verify(repository).findByCharacterId(characterId);
    }

    @Test
    void findByOrganizationId_deveDelegarAoRepositorio() {
        UUID organizationId = UUID.randomUUID();
        List<CharacterOrganization> expected = List.of(new CharacterOrganization());
        when(repository.findByOrganizationId(organizationId)).thenReturn(expected);

        List<CharacterOrganization> result = service.findByOrganizationId(organizationId);

        assertEquals(expected, result);
        verify(repository).findByOrganizationId(organizationId);
    }

}
