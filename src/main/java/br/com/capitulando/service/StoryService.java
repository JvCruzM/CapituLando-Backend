package br.com.capitulando.service;

import br.com.capitulando.model.Story;
import br.com.capitulando.repository.StoryRepository; // Lembre-se de criar a interface JpaRepository
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StoryService {

    private final StoryRepository repository;

    public StoryService(StoryRepository repository) {
        this.repository = repository;
    }

    // SALVAR
    public Story save(Story story) {
        // O status já vem como DRAFT por padrão do seu modelo, mas garantimos as datas aqui
        story.setCreatedAt(Instant.now());
        story.setUpdatedAt(Instant.now());
        return repository.save(story);
    }

    // LISTAR TODAS
    public List<Story> findAll() {
        return repository.findAll();
    }

    // BUSCAR POR ID
    public Story findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("História não encontrada para o ID: " + id));
    }

    // BUSCAR POR AUTOR (PROFILE ID)
    public List<Story> findByProfileId(UUID profileId) {
        return repository.findByProfileId(profileId);
    }

    // ATUALIZAR
    public Story update(UUID id, Story storyDetails) {
        Story existingStory = findById(id);

        existingStory.setTitle(storyDetails.getTitle());
        existingStory.setSynopsis(storyDetails.getSynopsis());
        existingStory.setDescription(storyDetails.getDescription());
        existingStory.setGenre(storyDetails.getGenre());
        existingStory.setCoverImage(storyDetails.getCoverImage());
        
        // Atualiza o status (ex: de DRAFT para PUBLISHED) se for enviado
        if (storyDetails.getStatus() != null) {
            existingStory.setStatus(storyDetails.getStatus());
        }

        // Só atualiza o perfil autor se ele vier na requisição (transferência de autoria, por exemplo)
        if (storyDetails.getProfile() != null) {
            existingStory.setProfile(storyDetails.getProfile());
        }

        existingStory.setUpdatedAt(Instant.now());

        return repository.save(existingStory);
    }

    // DELETAR
    public void delete(UUID id) {
        Story existingStory = findById(id);
        repository.delete(existingStory);
    }
}