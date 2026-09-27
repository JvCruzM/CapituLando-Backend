package br.com.capitulando.service;

import br.com.capitulando.model.StoryStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StoryStatusService {

    // Retorna todos os valores possíveis do Enum
    public List<StoryStatus> findAll() {
        return Arrays.asList(StoryStatus.values());
    }
}