package br.com.capitulando.repository;

import br.com.capitulando.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByStoryId(UUID storyId);
    List<Location> findByParentLocationId(UUID parentLocationId);
}
