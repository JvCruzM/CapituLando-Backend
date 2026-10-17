package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.Story;
import br.com.capitulando.model.StoryStatus;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.StoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StoryService {

    private final StoryRepository repository;
    private final ProfileRepository profileRepository;

    public StoryService(
            StoryRepository repository,
            ProfileRepository profileRepository) {
        this.repository = repository;
        this.profileRepository = profileRepository;
    }

    public Story save(UUID profileId, Story story) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil do usuário autenticado não encontrado."));

        // A autoria vem da identidade autenticada, não do corpo da requisição.
        story.setId(null);
        story.setProfile(profile);

        Instant now = Instant.now();

        story.setCreatedAt(now);
        story.setUpdatedAt(now);

        if (story.getStatus() == null) {
            story.setStatus(StoryStatus.DRAFT);
        }

        if (story.getProgressPercentage() == null) {
            story.setProgressPercentage(0);
        }

        return repository.save(story);
    }

    public List<Story> findAll(UUID profileId) {
        return repository.findAllByProfileId(profileId);
    }

    public Story findById(UUID id, UUID profileId) {
        return repository.findByIdAndProfileId(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "História não encontrada."));
    }

    public Story update(UUID id, UUID profileId, Story storyDetails) {
        Story existingStory = findById(id, profileId);

        existingStory.setTitle(storyDetails.getTitle());
        existingStory.setSynopsis(storyDetails.getSynopsis());
        existingStory.setDescription(storyDetails.getDescription());
        existingStory.setGenre(storyDetails.getGenre());
        existingStory.setCoverImage(storyDetails.getCoverImage());

        if (storyDetails.getStatus() != null) {
            existingStory.setStatus(storyDetails.getStatus());
        }

        // O perfil proprietário não pode ser transferido pelo corpo da requisição.
        existingStory.setUpdatedAt(Instant.now());

        return repository.save(existingStory);
    }

    public void delete(UUID id, UUID profileId) {
        Story existingStory = findById(id, profileId);
        repository.delete(existingStory);
    }
}