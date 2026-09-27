package br.com.capitulando.repository;

import br.com.capitulando.model.CharacterOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CharacterOrganizationRepository extends JpaRepository<CharacterOrganization, UUID> {
    List<CharacterOrganization> findByCharacterId(UUID characterId);
    List<CharacterOrganization> findByOrganizationId(UUID organizationId);
}
