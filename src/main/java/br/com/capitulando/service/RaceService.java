package br.com.capitulando.service;

import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.RaceRepository;
import br.com.capitulando.repository.StoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RaceService {

    private final RaceRepository repository;
    private final StoryRepository storyRepository;

    public RaceService(
            RaceRepository repository,
            StoryRepository storyRepository) {
        this.repository = repository;
        this.storyRepository = storyRepository;
    }

    public Race save(UUID profileId, Race race) {
        if (race.getStory() == null
                || race.getStory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "História não encontrada.");
        }

        Story story = findOwnedStory(
                race.getStory().getId(),
                profileId);

        // A raça e sua história são definidas após validar a propriedade.
        race.setId(null);
        race.setStory(story);

        Instant now = Instant.now();
        race.setCreatedAt(now);
        race.setUpdatedAt(now);

        Race saved = repository.save(race);

        return findById(saved.getId(), profileId);
    }

    public List<Race> findAll(UUID profileId) {
        return repository.findAllByStory_Profile_Id(profileId);
    }

    public Race findById(UUID id, UUID profileId) {
        return repository.findByIdAndStory_Profile_Id(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Raça não encontrada."));
    }

    public List<Race> findByStoryId(
            UUID storyId,
            UUID profileId) {
        // Não retornar uma lista vazia quando a história é alheia:
        // primeiro verificamos se o usuário tem acesso a ela.
        findOwnedStory(storyId, profileId);

        return repository.findByStoryIdAndStory_Profile_Id(
                storyId,
                profileId);
    }

    public Race update(
            UUID id,
            UUID profileId,
            Race raceDetails) {
        Race existingRace = findById(id, profileId);

        // Validar a história de destino antes de modificar qualquer campo.
        Story targetStory = null;

        if (raceDetails.getStory() != null) {
            UUID storyId = raceDetails.getStory().getId();

            if (storyId == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "História não encontrada.");
            }

            targetStory = findOwnedStory(storyId, profileId);
        }

        existingRace.setName(raceDetails.getName());
        existingRace.setDescription(raceDetails.getDescription());
        existingRace.setAppearance(raceDetails.getAppearance());
        existingRace.setCulture(raceDetails.getCulture());
        existingRace.setHabitat(raceDetails.getHabitat());
        existingRace.setRaceImage(raceDetails.getRaceImage());

        if (targetStory != null) {
            existingRace.setStory(targetStory);
        }

        existingRace.setUpdatedAt(Instant.now());

        repository.save(existingRace);

        return findById(id, profileId);
    }

    public void delete(UUID id, UUID profileId) {
        Race existingRace = findById(id, profileId);
        repository.delete(existingRace);
    }

    private Story findOwnedStory(UUID storyId, UUID profileId) {
        return storyRepository.findByIdAndProfileId(storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "História não encontrada."));
    }
}