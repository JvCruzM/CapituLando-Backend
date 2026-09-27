package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    // SALVAR
    public Profile save(Profile profile) {
        if (profile.getId() == null) {
            throw new IllegalArgumentException(
                    "O ID do perfil deve corresponder ao ID do usuário autenticado.");
        }

        Instant now = Instant.now();

        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(now);
        }

        profile.setUpdatedAt(now);

        return repository.save(profile);
    }

    // LISTAR TODOS
    public List<Profile> findAll() {
        return repository.findAll();
    }

    // BUSCAR POR ID
    public Profile findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para o ID: " + id));
    }

    // BUSCAR POR USERNAME
    public Profile findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para o username: " + username));
    }

    // ATUALIZAR
    public Profile update(UUID id, Profile profileDetails) {
        Profile existingProfile = findById(id);

        existingProfile.setUsername(profileDetails.getUsername());
        existingProfile.setDisplayName(profileDetails.getDisplayName());
        existingProfile.setBio(profileDetails.getBio());
        existingProfile.setAvatarImage(profileDetails.getAvatarImage());

        // Atualiza a data de modificação
        existingProfile.setUpdatedAt(Instant.now());

        return repository.save(existingProfile);
    }

    // DELETAR
    public void delete(UUID id) {
        Profile existingProfile = findById(id);
        repository.delete(existingProfile);
    }
}