package br.com.capitulando.service;

import br.com.capitulando.model.CharacterOrganization;
import br.com.capitulando.repository.CharacterOrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CharacterOrganizationService {

    private final CharacterOrganizationRepository repository;

    public CharacterOrganizationService(CharacterOrganizationRepository repository) {
        this.repository = repository;
    }

    public CharacterOrganization save(CharacterOrganization characterOrganization) {
        return repository.save(characterOrganization);
    }

    public List<CharacterOrganization> findAll() {
        return repository.findAll();
    }

    public CharacterOrganization findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relação Personagem-Organização não encontrada para o ID: " + id));
    }

    public List<CharacterOrganization> findByCharacterId(UUID characterId) {
        return repository.findByCharacterId(characterId);
    }

    public List<CharacterOrganization> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    public CharacterOrganization update(UUID id, CharacterOrganization details) {
        CharacterOrganization existing = findById(id);

        existing.setRole(details.getRole());
        existing.setJoinedAt(details.getJoinedAt());
        if (details.getIsActive() != null) {
            existing.setIsActive(details.getIsActive());
        }

        if (details.getCharacter() != null) {
            existing.setCharacter(details.getCharacter());
        }

        if (details.getOrganization() != null) {
            existing.setOrganization(details.getOrganization());
        }

        return repository.save(existing);
    }

    public void delete(UUID id) {
        CharacterOrganization existing = findById(id);
        repository.delete(existing);
    }
}
