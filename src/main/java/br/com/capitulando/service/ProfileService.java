package br.com.capitulando.service;

import br.com.capitulando.model.Profile;
import br.com.capitulando.model.User;
import br.com.capitulando.repository.ProfileRepository;
import br.com.capitulando.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository repository;
    private final UserRepository userRepository;

    public ProfileService(
            ProfileRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Profile save(UUID authenticatedUserId, Profile profile) {
        if (repository.existsById(authenticatedUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O perfil desta conta já existe.");
        }

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));

        profile.setId(authenticatedUserId);
        profile.setUser(user);

        Instant now = Instant.now();

        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(now);
        }

        profile.setUpdatedAt(now);

        return repository.save(profile);
    }

    public List<Profile> findAll() {
        return repository.findAll();
    }

    public Profile findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para o ID: " + id));
    }

    public Profile findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para o username: " + username));
    }

    public Profile update(
            UUID id,
            UUID authenticatedUserId,
            Profile profileDetails) {
        Profile existingProfile = findOwnedProfile(id, authenticatedUserId);

        existingProfile.setUsername(profileDetails.getUsername());
        existingProfile.setDisplayName(profileDetails.getDisplayName());
        existingProfile.setBio(profileDetails.getBio());
        existingProfile.setAvatarImage(profileDetails.getAvatarImage());

        existingProfile.setUpdatedAt(Instant.now());

        return repository.save(existingProfile);
    }

    public void delete(UUID id, UUID authenticatedUserId) {
        Profile existingProfile = findOwnedProfile(id, authenticatedUserId);
        repository.delete(existingProfile);
    }

    private Profile findOwnedProfile(UUID id, UUID authenticatedUserId) {
        return repository.findByIdAndUser_Id(id, authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil não encontrado."));
    }
}
