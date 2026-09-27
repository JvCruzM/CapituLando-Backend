package br.com.capitulando.service;

import br.com.capitulando.model.Race;
import br.com.capitulando.repository.RaceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RaceService {

    private final RaceRepository repository;

    public RaceService(RaceRepository repository) {
        this.repository = repository;
    }

    public Race save(Race race) {
        Instant now = Instant.now();

        if (race.getCreatedAt() == null) {
            race.setCreatedAt(now);
        }

        race.setUpdatedAt(now);

        return repository.save(race);
    }

    public List<Race> findAll() {
        return repository.findAll();
    }

    public Race findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Raça não encontrada para o ID: " + id));
    }

    public Race update(UUID id, Race raceDetails) {
        Race existingRace = findById(id);

        existingRace.setName(raceDetails.getName());
        existingRace.setDescription(raceDetails.getDescription());
        existingRace.setAppearance(raceDetails.getAppearance());
        existingRace.setCulture(raceDetails.getCulture());
        existingRace.setHabitat(raceDetails.getHabitat());
        existingRace.setRaceImage(raceDetails.getRaceImage());

        if (raceDetails.getStory() != null) {
            existingRace.setStory(raceDetails.getStory());
        }

        existingRace.setUpdatedAt(Instant.now());

        return repository.save(existingRace);
    }

    public void delete(UUID id) {
        Race existingRace = findById(id);
        repository.delete(existingRace);
    }
}