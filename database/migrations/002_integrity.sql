-- CapituLando

-- Migration 002: integridade entre Story, Race, Character e Creature

-- Precisamos de uma chave única composta em races para que
-- story_id + id possa ser referenciado por uma FK composta.

ALTER TABLE public.races
    ADD CONSTRAINT races_story_id_id_key
    UNIQUE (story_id, id);


-- Um personagem só pode utilizar uma raça pertencente
-- à mesma história do personagem.

ALTER TABLE public.characters
    ADD CONSTRAINT characters_story_race_fkey
    FOREIGN KEY (story_id, race_id)
    REFERENCES public.races (story_id, id)
    ON DELETE NO ACTION;


-- Uma criatura só pode utilizar uma raça pertencente
-- à mesma história da criatura.

ALTER TABLE public.creatures
    ADD CONSTRAINT creatures_story_race_fkey
    FOREIGN KEY (story_id, race_id)
    REFERENCES public.races (story_id, id)
    ON DELETE NO ACTION;