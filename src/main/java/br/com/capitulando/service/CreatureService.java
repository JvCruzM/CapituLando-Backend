package br.com.capitulando.service;

import br.com.capitulando.model.Creature;
import br.com.capitulando.model.Race;
import br.com.capitulando.model.Story;
import br.com.capitulando.repository.CreatureRepository;
import br.com.capitulando.repository.RaceRepository;
import br.com.capitulando.repository.StoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CreatureService {

    private final CreatureRepository repository;
    private final StoryRepository storyRepository;
    private final RaceRepository raceRepository;

    public CreatureService(
            CreatureRepository repository,
            StoryRepository storyRepository,
            RaceRepository raceRepository) {
        this.repository = repository;
        this.storyRepository = storyRepository;
        this.raceRepository = raceRepository;
    }

    public Creature save(UUID profileId, Creature creature) {
        if (creature.getStory() == null
                || creature.getStory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "História não encontrada.");
        }

        Story story = findOwnedStory(
                creature.getStory().getId(),
                profileId);

        Race race = null;

        if (creature.getRace() != null) {
            UUID raceId = creature.getRace().getId();

            if (raceId == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Raça não encontrada nesta história.");
            }

            race = findOwnedRace(
                    raceId,
                    story.getId(),
                    profileId);
        }

        creature.setId(null);
        creature.setStory(story);
        creature.setRace(race);

        Instant now = Instant.now();
        creature.setCreatedAt(now);
        creature.setUpdatedAt(now);

        Creature saved = repository.save(creature);

        return findById(saved.getId(), profileId);
    }

    public List<Creature> findAll(UUID profileId) {
        return repository.findAllByStory_Profile_Id(profileId);
    }

    public Creature findById(UUID id, UUID profileId) {
        return repository.findByIdAndStory_Profile_Id(id, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Criatura não encontrada."));
    }

    public List<Creature> findByStoryId(
            UUID storyId,
            UUID profileId) {
        findOwnedStory(storyId, profileId);

        return repository.findByStoryIdAndStory_Profile_Id(
                storyId,
                profileId);
    }

    public Creature update(
            UUID id,
            UUID profileId,
            Creature creatureDetails) {
        Creature existingCreature = findById(id, profileId);

        Story targetStory = existingCreature.getStory();

        if (creatureDetails.getStory() != null) {
            UUID storyId = creatureDetails.getStory().getId();

            if (storyId == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "História não encontrada.");
            }

            targetStory = findOwnedStory(storyId, profileId);
        }

        Race targetRace = existingCreature.getRace();

        if (creatureDetails.getRace() != null) {
            UUID raceId = creatureDetails.getRace().getId();

            if (raceId == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Raça não encontrada nesta história.");
            }

            targetRace = findOwnedRace(
                    raceId,
                    targetStory.getId(),
                    profileId);
        } else if (targetRace != null) {
            targetRace = findOwnedRace(
                    targetRace.getId(),
                    targetStory.getId(),
                    profileId);
        }

        existingCreature.setName(creatureDetails.getName());
        existingCreature.setDescription(
                creatureDetails.getDescription());
        existingCreature.setAppearance(
                creatureDetails.getAppearance());
        existingCreature.setBehavior(creatureDetails.getBehavior());
        existingCreature.setHabitat(creatureDetails.getHabitat());
        existingCreature.setCreatureImage(
                creatureDetails.getCreatureImage());

        existingCreature.setStory(targetStory);
        existingCreature.setRace(targetRace);
        existingCreature.setUpdatedAt(Instant.now());

        repository.save(existingCreature);

        return findById(id, profileId);
    }

    public void delete(UUID id, UUID profileId) {
        Creature existingCreature = findById(id, profileId);
        repository.delete(existingCreature);
    }

    private Story findOwnedStory(UUID storyId, UUID profileId) {
        return storyRepository.findByIdAndProfileId(storyId, profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "História não encontrada."));
    }

    private Race findOwnedRace(
            UUID raceId,
            UUID storyId,
            UUID profileId) {
        return raceRepository
                .findByIdAndStory_IdAndStory_Profile_Id(
                        raceId,
                        storyId,
                        profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Raça não encontrada nesta história."));
    }
}