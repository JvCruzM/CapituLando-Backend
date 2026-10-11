package br.com.capitulando.repository;

import br.com.capitulando.model.EventCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventCharacterRepository extends JpaRepository<EventCharacter, UUID> {
    List<EventCharacter> findAllByEvent_Id(UUID eventId);

    List<EventCharacter> findAllByCharacter_Id(UUID characterId);
}
