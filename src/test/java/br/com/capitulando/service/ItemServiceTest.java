package br.com.capitulando.service;

import br.com.capitulando.model.Item;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.ItemRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private ItemService service;

    @Test
    void save_deveDefinirDatasEDelegarAoRepositorio() {
        Item entity = new Item();

        Instant before = Instant.now();
        UUID profileId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        Story story = new Story();
        story.setId(storyId);
        entity.setStory(story);
        when(storyRepository.findByIdAndProfileId(storyId, profileId))
                .thenReturn(Optional.of(story));
        when(repository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(repository.findByIdAndStory_Profile_Id(any(), eq(profileId)))
                .thenAnswer(invocation -> Optional.of(entity));

        Item result = service.save(profileId, entity);
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
        Item entity = new Item();
        List<Item> expected = List.of(entity);
        when(repository.findAllByStory_Profile_Id(any())).thenReturn(expected);

        UUID profileId = UUID.randomUUID();
        List<Item> result = service.findAll(profileId);

        assertEquals(expected, result);
        verify(repository).findAllByStory_Profile_Id(profileId);
    }

    @Test
    void findById_deveRetornarEntidadeQuandoEncontrada() {
        UUID id = UUID.randomUUID();
        Item entity = new Item();
        entity.setId(id);
        UUID profileId = UUID.randomUUID();
        when(repository.findByIdAndStory_Profile_Id(id, profileId)).thenReturn(Optional.of(entity));

        Item result = service.findById(id, profileId);

        assertSame(entity, result);
        verify(repository).findByIdAndStory_Profile_Id(id, profileId);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(repository.findByIdAndStory_Profile_Id(id, profileId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(id, profileId));

        verify(repository).findByIdAndStory_Profile_Id(id, profileId);
    }

    @Test
    void update_deveAtualizarCampoESalvarEntidade() {
        UUID id = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(id);
        existing.setName("Valor antigo");
        Item details = new Item();
        details.setName("Item atualizado");
        UUID profileId = UUID.randomUUID();
        Story story = new Story();
        existing.setStory(story);
        when(repository.findByIdAndStory_Profile_Id(id, profileId)).thenReturn(Optional.of(existing));
        when(repository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.findByIdAndStory_Profile_Id(id, profileId)).thenReturn(Optional.of(existing));
        Item result = service.update(id, profileId, details);

        assertSame(existing, result);
        assertEquals("Item atualizado", result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(repository, atLeastOnce()).findByIdAndStory_Profile_Id(id, profileId);
        verify(repository).save(existing);
    }

    @Test
    void delete_deveExcluirEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(id);
        UUID profileId = UUID.randomUUID();
        when(repository.findByIdAndStory_Profile_Id(id, profileId)).thenReturn(Optional.of(existing));

        service.delete(id, profileId);

        verify(repository).findByIdAndStory_Profile_Id(id, profileId);
        verify(repository).delete(existing);
    }

    @Test
    void findByStoryId_deveDelegarAoRepositorio() {
        UUID storyId = UUID.randomUUID();
        List<Item> expected = List.of(new Item());
        UUID profileId = UUID.randomUUID();
        Story story = new Story();
        when(storyRepository.findByIdAndProfileId(storyId, profileId)).thenReturn(Optional.of(story));
        when(repository.findByStoryIdAndStory_Profile_Id(storyId, profileId)).thenReturn(expected);

        List<Item> result = service.findByStoryId(storyId, profileId);

        assertEquals(expected, result);
        verify(repository).findByStoryIdAndStory_Profile_Id(storyId, profileId);
    }

}
