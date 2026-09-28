package br.com.capitulando.service;

import br.com.capitulando.model.Location;
import br.com.capitulando.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
public class LocationService {

    private final LocationRepository repository;

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

    public Location save(Location location) {
        Instant now = Instant.now();

        if (location.getCreatedAt() == null) {
            location.setCreatedAt(now);
        }

        location.setUpdatedAt(now);

        return repository.save(location);
    }

    public List<Location> findAll() {
        return repository.findAll();
    }

    public Location findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Localização não encontrada para o ID: " + id));
    }

    public List<Location> findByStoryId(UUID storyId) {
        return repository.findByStoryId(storyId);
    }

    public List<Location> findByParentLocationId(UUID parentLocationId) {
        return repository.findByParentLocationId(parentLocationId);
    }

    public Location update(UUID id, Location locationDetails) {
        Location existingLocation = findById(id);

        existingLocation.setName(locationDetails.getName());
        existingLocation.setType(locationDetails.getType());
        existingLocation.setDescription(locationDetails.getDescription());
        existingLocation.setClimate(locationDetails.getClimate());
        existingLocation.setLocationImage(locationDetails.getLocationImage());

        if (locationDetails.getStory() != null) {
            existingLocation.setStory(locationDetails.getStory());
        }

        if (locationDetails.getParentLocation() != null) {
            existingLocation.setParentLocation(locationDetails.getParentLocation());
        }

        existingLocation.setUpdatedAt(Instant.now());

        return repository.save(existingLocation);
    }

    public void delete(UUID id) {
        Location existingLocation = findById(id);
        repository.delete(existingLocation);
    }
}
