package br.com.capitulando.service;

import br.com.capitulando.model.StoryStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoryStatusServiceTest {

    private final StoryStatusService service = new StoryStatusService();

    @Test
    void findAll_deveRetornarTodosOsStatusDisponiveis() {
        assertEquals(List.of(StoryStatus.values()), service.findAll());
    }
}
