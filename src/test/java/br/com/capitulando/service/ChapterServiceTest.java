package br.com.capitulando.service;

import br.com.capitulando.dto.ChapterRequest;
import br.com.capitulando.dto.ChapterResponse;
import br.com.capitulando.model.Chapter;
import br.com.capitulando.model.ChapterStatus;
import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.ChapterRepository;
import br.com.capitulando.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    @Mock
    private ChapterRepository repository;
    @Mock
    private StoryRepository storyRepository;
    @InjectMocks
    private ChapterService service;

    @Test
    void save_deveCriarCapituloVinculadoEContarPalavras() {
        UUID profileId = UUID.randomUUID();
        Story story = story(profileId);
        when(storyRepository.findByIdAndProfileId(story.getId(), profileId)).thenReturn(Optional.of(story));
        when(repository.findTopByStoryIdOrderByOrderIndexDesc(story.getId())).thenReturn(Optional.empty());
        when(repository.save(any(Chapter.class))).thenAnswer(invocation -> {
            Chapter chapter = invocation.getArgument(0);
            chapter.setId(UUID.randomUUID());
            return chapter;
        });

        ChapterResponse result = service.save(story.getId(), profileId,
                new ChapterRequest("  Primeiro capítulo  ", "Uma história começa aqui.", null, null));

        assertEquals(story.getId(), result.storyId());
        assertEquals(1, result.orderIndex());
        assertEquals("Primeiro capítulo", result.title());
        assertEquals(4, result.wordCount());
        assertEquals(ChapterStatus.DRAFT, result.status());
        assertNotNull(result.createdAt());
        verify(repository).save(any(Chapter.class));
    }

    @Test
    void save_deveRecusarHistoriaDeOutroPerfil() {
        UUID storyId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(storyRepository.findByIdAndProfileId(storyId, profileId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.save(storyId, profileId,
                new ChapterRequest("Capítulo", "Conteúdo", null, null)));
    }

    @Test
    void save_deveContarPalavrasDoConteudoFormatadoSemContarTags() {
        UUID profileId = UUID.randomUUID();
        Story story = story(profileId);
        when(storyRepository.findByIdAndProfileId(story.getId(), profileId)).thenReturn(Optional.of(story));
        when(repository.findTopByStoryIdOrderByOrderIndexDesc(story.getId())).thenReturn(Optional.empty());
        when(repository.save(any(Chapter.class))).thenAnswer(invocation -> {
            Chapter chapter = invocation.getArgument(0);
            chapter.setId(UUID.randomUUID());
            return chapter;
        });

        ChapterResponse result = service.save(story.getId(), profileId,
                new ChapterRequest("Capítulo", "<p>Uma</p><p>história com quatro palavras</p>", null, null));

        assertEquals(5, result.wordCount());
    }

    @Test
    void update_deveAtualizarSomenteCapituloDaHistoriaEPerfilInformados() {
        UUID profileId = UUID.randomUUID();
        Story story = story(profileId);
        UUID chapterId = UUID.randomUUID();
        Chapter existing = new Chapter();
        existing.setId(chapterId);
        existing.setStory(story);
        existing.setOrderIndex(1);
        existing.setTitle("Antigo");
        existing.setContent("texto velho");
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());
        when(repository.findByIdAndStoryIdAndStoryProfileId(chapterId, story.getId(), profileId))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ChapterResponse result = service.update(story.getId(), chapterId, profileId,
                new ChapterRequest("Novo", "  uma   história\ncom palavras  ", null, ChapterStatus.IN_REVIEW));

        assertEquals("Novo", result.title());
        assertEquals(4, result.wordCount());
        assertEquals(ChapterStatus.IN_REVIEW, result.status());
        verify(repository).save(existing);
    }

    private Story story(UUID profileId) {
        Profile profile = new Profile();
        profile.setId(profileId);
        Story story = new Story();
        story.setId(UUID.randomUUID());
        story.setProfile(profile);
        return story;
    }
}
