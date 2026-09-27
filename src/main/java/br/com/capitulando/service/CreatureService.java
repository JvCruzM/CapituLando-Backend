package br.com.capitulando.service;

import br.com.capitulando.model.Creature;
import br.com.capitulando.repository.CreatureRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CreatureService {

    private final CreatureRepository repository;

    // Construtor: o Spring injeta o Repository automaticamente aqui
    public CreatureService(CreatureRepository repository) {
        this.repository = repository;
    }

    // SALVAR
    public Creature save(Creature creature) {
        // Preenche as datas automaticamente antes de salvar
        creature.setCreatedAt(Instant.now());
        creature.setUpdatedAt(Instant.now());
        return repository.save(creature);
    }

    // LISTAR TODOS
    public List<Creature> findAll() {
        return repository.findAll();
    }

    // BUSCAR POR ID
    public Creature findById(UUID id) {
        // O Optional lida com o caso de "E se a criatura não for encontrada?"
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Criatura não encontrada para o ID: " + id));
    }

    // ATUALIZAR
    public Creature update(UUID id, Creature creatureDetails) {
        // 1. Busca a criatura existente no banco (ou lança erro se não achar)
        Creature existingCreature = findById(id); 

        // 2. Atualiza apenas os dados permitidos
        existingCreature.setName(creatureDetails.getName());
        existingCreature.setDescription(creatureDetails.getDescription());
        existingCreature.setAppearance(creatureDetails.getAppearance());
        existingCreature.setBehavior(creatureDetails.getBehavior());
        existingCreature.setHabitat(creatureDetails.getHabitat());
        existingCreature.setCreatureImage(creatureDetails.getCreatureImage());
        
        // Atualiza os relacionamentos (se o usuário mandar na requisição)
        if (creatureDetails.getStory() != null) {
            existingCreature.setStory(creatureDetails.getStory());
        }
        if (creatureDetails.getRace() != null) {
            existingCreature.setRace(creatureDetails.getRace());
        }

        // 3. Atualiza a data de modificação
        existingCreature.setUpdatedAt(Instant.now());

        // 4. Salva a criatura com os novos dados
        return repository.save(existingCreature);
    }

    // DELETAR
    public void delete(UUID id) {
        // Busca primeiro para garantir que existe, depois deleta
        Creature existingCreature = findById(id);
        repository.delete(existingCreature);
    }
}