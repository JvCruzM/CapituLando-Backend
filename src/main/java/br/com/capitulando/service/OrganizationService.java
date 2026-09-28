package br.com.capitulando.service;

import br.com.capitulando.model.Organization;
import br.com.capitulando.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
public class OrganizationService {

    private final OrganizationRepository repository;

    public OrganizationService(OrganizationRepository repository) {
        this.repository = repository;
    }

    public Organization save(Organization organization) {
        Instant now = Instant.now();

        if (organization.getCreatedAt() == null) {
            organization.setCreatedAt(now);
        }

        organization.setUpdatedAt(now);

        return repository.save(organization);
    }

    public List<Organization> findAll() {
        return repository.findAll();
    }

    public Organization findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organização não encontrada para o ID: " + id));
    }

    public List<Organization> findByStoryId(UUID storyId) {
        return repository.findByStoryId(storyId);
    }

    public Organization update(UUID id, Organization details) {
        Organization existing = findById(id);

        existing.setName(details.getName());
        existing.setType(details.getType());
        existing.setDescription(details.getDescription());
        existing.setOrganizationImage(details.getOrganizationImage());

        if (details.getStory() != null) {
            existing.setStory(details.getStory());
        }

        if (details.getHeadquartersLocation() != null) {
            existing.setHeadquartersLocation(details.getHeadquartersLocation());
        }

        existing.setUpdatedAt(Instant.now());

        return repository.save(existing);
    }

    public void delete(UUID id) {
        Organization existing = findById(id);
        repository.delete(existing);
    }
}
