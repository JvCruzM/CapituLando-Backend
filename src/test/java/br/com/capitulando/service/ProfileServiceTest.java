package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository repository;

    @InjectMocks
    private ProfileService service;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("joao");
        profile.setDisplayName("João");
        profile.setBio("Bio de teste");
        profile.setAvatarImage("avatar.png");
    }

    @Test
    void save_deveSalvarPerfilComDatasPreenchidas() {
        when(repository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant antes = Instant.now();

        Profile result = service.save(profile);

        Instant depois = Instant.now();

        assertSame(profile, result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        assertFalse(result.getCreatedAt().isBefore(antes));
        assertFalse(result.getCreatedAt().isAfter(depois));

        assertFalse(result.getUpdatedAt().isBefore(antes));
        assertFalse(result.getUpdatedAt().isAfter(depois));

        verify(repository).save(profile);
    }

    @Test
    void save_naoDeveAlterarCreatedAtQuandoJaExistir() {
        Instant createdAtOriginal = Instant.parse("2026-01-01T10:00:00Z");
        profile.setCreatedAt(createdAtOriginal);

        when(repository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = service.save(profile);

        assertEquals(createdAtOriginal, result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(repository).save(profile);
    }

    @Test
    void save_deveLancarExcecaoQuandoIdNaoForInformado() {
        profile.setId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.save(profile)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void findAll_deveRetornarTodosOsPerfis() {
        List<Profile> profiles = List.of(profile);

        when(repository.findAll()).thenReturn(profiles);

        List<Profile> result = service.findAll();

        assertEquals(profiles, result);
        verify(repository).findAll();
    }

    @Test
    void findById_deveRetornarPerfilQuandoEncontrado() {
        UUID id = profile.getId();

        when(repository.findById(id)).thenReturn(Optional.of(profile));

        Profile result = service.findById(id);

        assertEquals(profile, result);
        verify(repository).findById(id);
    }

    @Test
    void findById_deveLancarExcecaoQuandoPerfilNaoForEncontrado() {
        UUID id = profile.getId();

        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.findById(id)
        );

        assertEquals(
                "Perfil não encontrado para o ID: " + id,
                exception.getMessage()
        );

        verify(repository).findById(id);
    }

    @Test
    void findByUsername_deveRetornarPerfilQuandoEncontrado() {
        String username = profile.getUsername();

        when(repository.findByUsername(username))
                .thenReturn(Optional.of(profile));

        Profile result = service.findByUsername(username);

        assertEquals(profile, result);
        verify(repository).findByUsername(username);
    }

    @Test
    void findByUsername_deveLancarExcecaoQuandoPerfilNaoForEncontrado() {
        String username = profile.getUsername();

        when(repository.findByUsername(username))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.findByUsername(username)
        );

        assertEquals(
                "Perfil não encontrado para o username: " + username,
                exception.getMessage()
        );

        verify(repository).findByUsername(username);
    }

    @Test
    void update_deveAtualizarDadosDoPerfil() {
        UUID id = profile.getId();

        Profile existingProfile = new Profile();
        existingProfile.setId(id);
        existingProfile.setUsername("antigo");
        existingProfile.setDisplayName("Nome antigo");
        existingProfile.setBio("Bio antiga");
        existingProfile.setAvatarImage("antigo.png");
        existingProfile.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        existingProfile.setUpdatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Profile profileDetails = new Profile();
        profileDetails.setUsername("novo");
        profileDetails.setDisplayName("Nome novo");
        profileDetails.setBio("Bio nova");
        profileDetails.setAvatarImage("novo.png");

        when(repository.findById(id)).thenReturn(Optional.of(existingProfile));
        when(repository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = service.update(id, profileDetails);

        assertEquals("novo", result.getUsername());
        assertEquals("Nome novo", result.getDisplayName());
        assertEquals("Bio nova", result.getBio());
        assertEquals("novo.png", result.getAvatarImage());

        assertNotNull(result.getUpdatedAt());
        assertEquals(
                Instant.parse("2026-01-01T10:00:00Z"),
                result.getCreatedAt()
        );

        verify(repository).findById(id);
        verify(repository).save(existingProfile);
    }

    @Test
    void update_deveLancarExcecaoQuandoPerfilNaoExistir() {
        UUID id = profile.getId();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.update(id, profile)
        );

        verify(repository).findById(id);
        verify(repository, never()).save(any(Profile.class));
    }

    @Test
    void delete_deveExcluirPerfilQuandoEncontrado() {
        UUID id = profile.getId();

        when(repository.findById(id)).thenReturn(Optional.of(profile));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(profile);
    }

    @Test
    void delete_naoDeveExcluirQuandoPerfilNaoExistir() {
        UUID id = profile.getId();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.delete(id)
        );

        verify(repository).findById(id);
        verify(repository, never()).delete(any(Profile.class));
    }
}