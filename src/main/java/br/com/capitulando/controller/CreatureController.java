package com.seunome.projeto.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/characters")
public class CharacterController {

    // 1. CRIAR: Recebe um JSON no corpo da requisição e cria um personagem
    @PostMapping
    public CharacterEntity create(@RequestBody CharacterEntity character) {
        // Ação: Mandar o Service salvar no banco
        return character; 
    }

    // 2. BUSCAR TODOS: Retorna uma lista com todos os personagens
    @GetMapping
    public List<CharacterEntity> listAll() {
        // Ação: Mandar o Service buscar tudo no banco
        return null; 
    }

    // 3. BUSCAR POR ID: O {id} na URL é capturado pelo @PathVariable
    // Exemplo de chamada: GET localhost:8080/characters/1
    @GetMapping("/{id}")
    public CharacterEntity findById(@PathVariable Long id) {
        // Ação: Mandar o Service buscar o personagem número 'id'
        return null;
    }

    // 4. ATUALIZAR: Precisa do ID na URL para saber quem atualizar, e do JSON no corpo com os novos dados
    // Exemplo de chamada: PUT localhost:8080/characters/1
    @PutMapping("/{id}")
    public CharacterEntity update(@PathVariable Long id, @RequestBody CharacterEntity updatedCharacter) {
        // Ação: Mandar o Service atualizar
        return updatedCharacter;
    }

    // 5. DELETAR: Precisa apenas do ID na URL
    // Exemplo de chamada: DELETE localhost:8080/characters/1
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // Ação: Mandar o Service deletar do banco
    }
}